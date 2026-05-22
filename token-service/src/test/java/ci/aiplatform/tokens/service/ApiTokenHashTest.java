package ci.aiplatform.tokens.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiTokenHashTest {
    @Test
    void sha256IsDeterministic() {
        String a = ApiTokenService.sha256("hello");
        String b = ApiTokenService.sha256("hello");
        assertEquals(a, b);
        assertEquals(64, a.length());
    }
}

