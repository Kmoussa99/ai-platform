package ci.aiplatform.chat.domain;

public record RAGSource(String documentId, String chunkId, String title,
                        Integer page, Float score, String excerpt) {}

