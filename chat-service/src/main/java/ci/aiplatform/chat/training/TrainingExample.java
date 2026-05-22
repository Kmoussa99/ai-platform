package ci.aiplatform.chat.training;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "training_examples")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainingExample {
    @Id @GeneratedValue private UUID id;
    private UUID conversationId;
    @Column(columnDefinition = "TEXT") private String instruction;
    @Column(columnDefinition = "TEXT") private String responseChosen;
    @Column(columnDefinition = "TEXT") private String responseRejected;
    private String domain;
    private Float qualityScore;
    private String reviewedBy;
    @Enumerated(EnumType.STRING) @Builder.Default
    private TrainingStatus status = TrainingStatus.RAW;
    private Instant createdAt;

    @PrePersist void p() { createdAt = Instant.now(); }
}

