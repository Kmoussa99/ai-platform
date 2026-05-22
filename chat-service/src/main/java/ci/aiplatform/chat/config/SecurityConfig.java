package ci.aiplatform.chat.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain f(ServerHttpSecurity http) {
        http.csrf(c -> c.disable())
            .authorizeExchange(a -> a
                .pathMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/ws/**").permitAll()
                .anyExchange().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> {}));
        return http.build();
    }
}

