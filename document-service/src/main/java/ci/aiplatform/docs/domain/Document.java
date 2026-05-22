package ci.aiplatform.docs.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {
    @Id @GeneratedValue private UUID id;
    private String filename;
    private String collection;
    private String status;            // PENDING, INDEXING, INDEXED, FAILED
    private Integer chunkCount;
    private Integer tokenCount;
    private String uploadedBy;
    private String accessLevel;       // L1/L2/L3
    @ElementCollection @Builder.Default
    private List<String> tags = new ArrayList<>();
    private String objectKey;         // MinIO key
    private Instant createdAt;
    private Instant indexedAt;

    @PrePersist void p() { createdAt = Instant.now(); }
}

