package ci.aiplatform.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
        List<String> roles = realm != null ? (List<String>) realm.getOrDefault("roles", List.of()) : List.of();
        return Map.of(
            "id", jwt.getSubject(),
            "username", jwt.getClaimAsString("preferred_username"),
            "email", jwt.getClaimAsString("email"),
            "department", jwt.getClaimAsString("department"),
            "team", jwt.getClaimAsString("team"),
            "data_access_level", jwt.getClaimAsString("data_access_level"),
            "roles", roles
        );
    }
}

