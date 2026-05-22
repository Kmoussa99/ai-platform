package ci.aiplatform.auth.audit;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_ts", columnList = "timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue private UUID id;
    @Column(name = "user_id") private String userId;
    private String username;
    private String action;
    private String resource;
    private String ip;
    private String userAgent;
    @Column(length = 2000) private String details;
    private boolean success;
    @Column(nullable = false) private Instant timestamp;
}

