package ci.aiplatform.tokens.service;

import ci.aiplatform.tokens.domain.ApiToken;
import ci.aiplatform.tokens.domain.Scope;
import ci.aiplatform.tokens.repository.ApiTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private static final SecureRandom RNG = new SecureRandom();
    private final ApiTokenRepository repo;

    public record CreatedToken(ApiToken meta, String plaintext) {}

    @Transactional
    public CreatedToken create(String userId, String name, Set<Scope> scopes,
                                Integer rateLimit, LocalDateTime expiresAt, List<String> allowedIps) {
        String plaintext = generatePlaintext();
        String hash = sha256(plaintext);
        ApiToken token = ApiToken.builder()
            .name(name).ownerUserId(userId)
            .tokenHash(hash)
            .prefix(plaintext.substring(0, 12))
            .scopes(scopes == null ? Set.of() : scopes)
            .rateLimit(rateLimit)
            .expiresAt(expiresAt)
            .allowedIps(allowedIps == null ? List.of() : allowedIps)
            .active(true)
            .build();
        return new CreatedToken(repo.save(token), plaintext);
    }

    @Transactional(readOnly = true)
    public List<ApiToken> listMine(String userId) {
        return repo.findAllByOwnerUserId(userId);
    }

    @Transactional
    public void revoke(UUID id, String userId) {
        repo.findById(id).filter(t -> t.getOwnerUserId().equals(userId)).ifPresent(t -> {
            t.setActive(false);
            repo.save(t);
        });
    }

    @Transactional
    public CreatedToken rotate(UUID id, String userId) {
        ApiToken existing = repo.findById(id)
            .filter(t -> t.getOwnerUserId().equals(userId))
            .orElseThrow();
        String plaintext = generatePlaintext();
        existing.setTokenHash(sha256(plaintext));
        existing.setPrefix(plaintext.substring(0, 12));
        return new CreatedToken(repo.save(existing), plaintext);
    }

    @Transactional
    public Optional<ApiToken> validate(String plaintext) {
        return repo.findByTokenHash(sha256(plaintext))
            .filter(ApiToken::isActive)
            .filter(t -> t.getExpiresAt() == null || t.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(t -> {
                t.setLastUsedAt(LocalDateTime.now());
                t.setUsageCount(t.getUsageCount() + 1);
                return repo.save(t);
            });
    }

    /* helpers */

    private String generatePlaintext() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return "aip_live_" + Base64UrlNoPad(buf);
    }

    private static String Base64UrlNoPad(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}

