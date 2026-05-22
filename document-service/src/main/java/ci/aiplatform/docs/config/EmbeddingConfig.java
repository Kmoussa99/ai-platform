package ci.aiplatform.docs.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel(@Value("${ai.ollama.base-url}") String url,
                                         @Value("${ai.ollama.embedding-model}") String model) {
        return OllamaEmbeddingModel.builder().baseUrl(url).modelName(model).build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(@Value("${ai.qdrant.host}") String h,
                                                       @Value("${ai.qdrant.port}") int p) {
        return QdrantEmbeddingStore.builder()
            .host(h).port(p)
            .collectionName("knowledge_internal")
            .build();
    }
}

