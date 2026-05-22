package ci.aiplatform.chat.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Value("${ai.ollama.base-url}") String ollamaUrl;
    @Value("${ai.ollama.model}")    String chatModel;
    @Value("${ai.ollama.embedding-model}") String embedModel;
    @Value("${ai.qdrant.host}")     String qdrantHost;
    @Value("${ai.qdrant.port}")     int qdrantPort;

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OllamaStreamingChatModel.builder()
            .baseUrl(ollamaUrl)
            .modelName(chatModel)
            .timeout(Duration.ofMinutes(2))
            .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
            .baseUrl(ollamaUrl)
            .modelName(embedModel)
            .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return QdrantEmbeddingStore.builder()
            .host(qdrantHost)
            .port(qdrantPort)
            .collectionName("knowledge_internal")
            .build();
    }
}

