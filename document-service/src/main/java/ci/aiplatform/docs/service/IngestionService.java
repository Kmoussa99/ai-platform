package ci.aiplatform.docs.service;

import ci.aiplatform.docs.domain.Document;
import ci.aiplatform.docs.repository.DocumentRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentRepository repo;
    private final MinioClient minio;
    private final EmbeddingModel embedder;
    private final EmbeddingStore<TextSegment> store;

    @Value("${minio.bucket}") private String bucket;
    @Value("${ai.chunk.size}")    private int chunkSize;
    @Value("${ai.chunk.overlap}") private int chunkOverlap;

    private final Tika tika = new Tika();

    private static final Pattern PII_EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PII_PHONE = Pattern.compile("(?:\\+225|00225)?\\s?[0-9]{2}\\s?[0-9]{2}\\s?[0-9]{2}\\s?[0-9]{2}\\s?[0-9]{2}");

    public Document upload(MultipartFile file, String collection, String accessLevel,
                           List<String> tags, String userId) throws Exception {
        ensureBucket();
        String key = "docs/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        minio.putObject(PutObjectArgs.builder()
            .bucket(bucket).object(key).stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType()).build());

        Document d = repo.save(Document.builder()
            .filename(file.getOriginalFilename())
            .collection(collection == null ? "knowledge_internal" : collection)
            .accessLevel(accessLevel == null ? "L1" : accessLevel)
            .status("PENDING").objectKey(key).uploadedBy(userId)
            .tags(tags == null ? List.of() : tags).build());

        indexAsync(d.getId(), file.getBytes());
        return d;
    }

    @Async
    public void indexAsync(UUID docId, byte[] bytes) {
        Document d = repo.findById(docId).orElseThrow();
        try {
            d.setStatus("INDEXING"); repo.save(d);
            String text = tika.parseToString(new ByteArrayInputStream(bytes));
            text = anonymize(text);
            List<String> chunks = chunk(text, chunkSize, chunkOverlap);
            int tokenTotal = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String c = chunks.get(i);
                Map<String, String> m = new HashMap<>();
                m.put("document_id", d.getId().toString());
                m.put("title", d.getFilename());
                m.put("page", String.valueOf(i));
                m.put("access_level", d.getAccessLevel());
                m.put("collection", d.getCollection());
                TextSegment seg = TextSegment.from(c, Metadata.from(m));
                Embedding emb = embedder.embed(seg).content();
                store.add(emb, seg);
                tokenTotal += c.length() / 4;
            }
            d.setChunkCount(chunks.size());
            d.setTokenCount(tokenTotal);
            d.setStatus("INDEXED");
            d.setIndexedAt(Instant.now());
            repo.save(d);
            log.info("indexed doc={} chunks={}", d.getId(), chunks.size());
        } catch (Exception e) {
            log.error("ingest fail", e);
            d.setStatus("FAILED"); repo.save(d);
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }

    /** Simple PII anonymization. */
    public static String anonymize(String text) {
        text = PII_EMAIL.matcher(text).replaceAll("[EMAIL]");
        text = PII_PHONE.matcher(text).replaceAll("[PHONE]");
        return text;
    }

    /** Naive sliding-window character chunking (~tokens). */
    public static List<String> chunk(String text, int size, int overlap) {
        int approxChars = size * 4; // ~4 chars / token
        int step = Math.max(1, approxChars - overlap * 4);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < text.length(); i += step) {
            out.add(text.substring(i, Math.min(text.length(), i + approxChars)));
        }
        return out;
    }
}

