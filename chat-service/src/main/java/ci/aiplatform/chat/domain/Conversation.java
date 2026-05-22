package ci.aiplatform.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations", indexes = @Index(name = "idx_conv_user", columnList = "userId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {
    @Id @GeneratedValue private UUID id;
    private String title;
    @Column(nullable = false) private String userId;
    private String modelId;
    @Column(length = 4000) private String systemPrompt;
    @Enumerated(EnumType.STRING) @Builder.Default private ContextMode contextMode = ContextMode.GENERAL;
    @Builder.Default private boolean shared = false;
    @Builder.Default private boolean pinned = false;
    @Builder.Default private int messageCount = 0;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void p() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void u() { updatedAt = Instant.now(); }
}

