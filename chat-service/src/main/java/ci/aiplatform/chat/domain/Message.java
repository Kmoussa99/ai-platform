package ci.aiplatform.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages", indexes = @Index(name = "idx_msg_conv", columnList = "conversationId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID conversationId;
    @Enumerated(EnumType.STRING) private MessageRole role;
    @Column(columnDefinition = "TEXT") private String content;
    private Integer tokensUsed;
    private String modelUsed;
    private Integer latencyMs;
    @Column(columnDefinition = "TEXT") private String sourcesJson;
    @Column(columnDefinition = "TEXT") private String toolCallsJson;
    @Enumerated(EnumType.STRING) private Feedback feedback;
    private Instant createdAt;

    @PrePersist void p() { createdAt = Instant.now(); }
}

