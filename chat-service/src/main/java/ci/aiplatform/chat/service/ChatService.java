package ci.aiplatform.chat.service;

import ci.aiplatform.chat.domain.*;
import ci.aiplatform.chat.rag.RagPipeline;
import ci.aiplatform.chat.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;
    private final RagPipeline rag;
    private final StreamingChatLanguageModel llm;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public Conversation createConversation(String userId, String title, ContextMode mode) {
        return convRepo.save(Conversation.builder()
            .userId(userId).title(title == null ? "Nouvelle conversation" : title)
            .contextMode(mode == null ? ContextMode.GENERAL : mode)
            .build());
    }

    @Transactional(readOnly = true)
    public List<Conversation> listConversations(String userId) {
        return convRepo.findAllByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(UUID conversationId) {
        return msgRepo.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Streaming chat: persists user message, runs RAG, streams LLM tokens, persists assistant message.
     */
    public Flux<String> stream(UUID conversationId, String userId, String accessLevel, String userInput) {
        Conversation conv = convRepo.findById(conversationId).orElseThrow();
        if (!conv.getUserId().equals(userId)) throw new SecurityException("forbidden");

        long start = System.currentTimeMillis();
        Message userMsg = msgRepo.save(Message.builder()
            .conversationId(conversationId).role(MessageRole.USER).content(userInput).build());

        List<RAGSource> sources = rag.retrieve(userInput, accessLevel);
        String contextBlock = renderSources(sources);
        String systemPrompt = buildSystemPrompt(conv, contextBlock);

        List<ChatMessage> history = new ArrayList<>();
        history.add(SystemMessage.from(systemPrompt));
        msgRepo.findAllByConversationIdOrderByCreatedAtAsc(conversationId).forEach(m -> {
            switch (m.getRole()) {
                case USER -> history.add(UserMessage.from(m.getContent()));
                case ASSISTANT -> history.add(AiMessage.from(m.getContent()));
                default -> {}
            }
        });

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        StringBuilder full = new StringBuilder();

        llm.generate(history, new StreamingResponseHandler<>() {
            @Override public void onNext(String token) {
                full.append(token);
                sink.tryEmitNext(token);
            }
            @Override public void onComplete(Response<AiMessage> response) {
                int latency = (int) (System.currentTimeMillis() - start);
                try {
                    Message assistant = Message.builder()
                        .conversationId(conversationId)
                        .role(MessageRole.ASSISTANT)
                        .content(full.toString())
                        .modelUsed("ollama")
                        .latencyMs(latency)
                        .tokensUsed(response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : null)
                        .sourcesJson(mapper.writeValueAsString(sources))
                        .build();
                    msgRepo.save(assistant);
                    conv.setMessageCount(conv.getMessageCount() + 2);
                    convRepo.save(conv);
                } catch (Exception e) { log.error("persist fail", e); }
                sink.tryEmitComplete();
            }
            @Override public void onError(Throwable error) {
                log.error("llm stream error", error);
                sink.tryEmitError(error);
            }
        });
        return sink.asFlux();
    }

    @Transactional
    public void feedback(UUID messageId, Feedback feedback) {
        msgRepo.findById(messageId).ifPresent(m -> {
            m.setFeedback(feedback);
            msgRepo.save(m);
        });
    }

    private String buildSystemPrompt(Conversation c, String ctx) {
        String base = c.getSystemPrompt() != null ? c.getSystemPrompt()
            : "Tu es l'assistant IA interne de l'entreprise. Réponds en français, cite tes sources [1][2].";
        if (ctx.isEmpty()) return base;
        return base + "\n\n## Contexte (sources):\n" + ctx + "\n\nUtilise ces sources pour répondre. Cite-les avec [n].";
    }

    private String renderSources(List<RAGSource> sources) {
        if (sources.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            sb.append("[").append(i + 1).append("] ")
              .append(sources.get(i).title()).append(" — ")
              .append(sources.get(i).excerpt()).append("\n");
        }
        return sb.toString();
    }
}

