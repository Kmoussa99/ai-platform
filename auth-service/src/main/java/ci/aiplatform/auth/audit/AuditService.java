package ci.aiplatform.auth.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repo;

    @Async
    public void log(String userId, String username, String action, String resource,
                    String ip, String ua, String details, boolean success) {
        repo.save(AuditLog.builder()
            .userId(userId).username(username).action(action).resource(resource)
            .ip(ip).userAgent(ua).details(details).success(success)
            .timestamp(Instant.now()).build());
    }
}

