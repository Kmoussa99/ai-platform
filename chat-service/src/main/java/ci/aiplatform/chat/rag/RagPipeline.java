package ci.aiplatform.chat.rag;

import ci.aiplatform.chat.domain.RAGSource;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Pipeline RAG :
 * 1) query expansion (3 variantes simples)
 * 2) embeddings (Ollama nomic-embed-text)
 * 3) recherche dense Qdrant + filtrage data_access_level
 * 4) top-K fusion
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagPipeline {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${ai.rag.top-k:5}")
    private int topK;

    public List<RAGSource> retrieve(String query, String accessLevel) {
        List<String> expansions = expand(query);
        Map<String, RAGSource> dedup = new LinkedHashMap<>();
        for (String q : expansions) {
            try {
                Embedding emb = embeddingModel.embed(q).content();
                EmbeddingSearchRequest req = EmbeddingSearchRequest.builder()
                    .queryEmbedding(emb).maxResults(topK).minScore(0.5).build();
                for (EmbeddingMatch<TextSegment> m : embeddingStore.search(req).matches()) {
                    String level = m.embedded().metadata().getString("access_level");
                    if (!hasAccess(accessLevel, level)) continue;
                    String id = m.embeddingId();
                    dedup.putIfAbsent(id, new RAGSource(
                        m.embedded().metadata().getString("document_id"),
                        id,
                        m.embedded().metadata().getString("title"),
                        parseInt(m.embedded().metadata().getString("page")),
                        m.score().floatValue(),
                        truncate(m.embedded().text(), 280)
                    ));
                }
            } catch (Exception ex) {
                // RAG is best-effort: missing collection, Qdrant down, embedding model not pulled, etc.
                // shouldn't break the chat — degrade gracefully to a no-context answer.
                log.warn("RAG retrieve failed (degrading to no-context): {}", ex.getMessage());
                return List.of();
            }
        }
        return dedup.values().stream().limit(topK).toList();
    }

    private List<String> expand(String q) {
        return List.of(q,
            "Question : " + q,
            "Reformulation : " + q + " (donne contexte et exemples)");
    }

    private boolean hasAccess(String userLevel, String docLevel) {
        if (docLevel == null || docLevel.isBlank()) return true;
        int u = parseLevel(userLevel), d = parseLevel(docLevel);
        return u >= d;
    }
    private int parseLevel(String s) {
        if (s == null) return 1;
        return switch (s.toUpperCase()) { case "L3" -> 3; case "L2" -> 2; default -> 1; };
    }
    private Integer parseInt(String s) { try { return s == null ? null : Integer.parseInt(s); } catch (Exception e) { return null; } }
    private String truncate(String s, int n) { return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "..."); }
}

