package ci.aiplatform.tokens.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "api_tokens", indexes = {
    @Index(name = "idx_token_hash", columnList = "tokenHash", unique = true),
    @Index(name = "idx_token_owner", columnList = "ownerUserId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiToken {
    @Id @GeneratedValue private UUID id;

    @Column(nullable = false) private String name;

    /** SHA-256 hash of the token, never the plaintext. */
    @Column(nullable = false, unique = true, length = 128)
    private String tokenHash;

    /** Public prefix to help users identify the token (e.g. "aip_live_xxxxx..."). */
    @Column(nullable = false, length = 16)
    private String prefix;

    @Column(nullable = false) private String ownerUserId;

    @ElementCollection(targetClass = Scope.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "api_token_scopes", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "scope")
    @Builder.Default
    private Set<Scope> scopes = new HashSet<>();

    private Integer rateLimit;
    private LocalDateTime expiresAt;
    @Builder.Default private boolean active = true;
    private LocalDateTime lastUsedAt;
    @Builder.Default private long usageCount = 0;

    @ElementCollection
    @CollectionTable(name = "api_token_ips", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "cidr")
    @Builder.Default
    private List<String> allowedIps = new ArrayList<>();

    @Column(nullable = false) private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}

