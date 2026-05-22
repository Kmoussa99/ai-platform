package ci.aiplatform.gateway.filter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

class DlpPatternsTest {
    @Test
    void detectEmail() {
        assertTrue(Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
            .matcher("contact: john@acme.ci").find());
    }
    @Test
    void detectAwsKey() {
        assertTrue(Pattern.compile("AKIA[0-9A-Z]{16}")
            .matcher("key=AKIAABCDEFGHIJKLMNOP").find());
    }
}

