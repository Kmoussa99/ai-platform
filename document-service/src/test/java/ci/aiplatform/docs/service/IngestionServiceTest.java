package ci.aiplatform.docs.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IngestionServiceTest {
    @Test
    void anonymizeEmailAndPhone() {
        String r = IngestionService.anonymize("Contact john@acme.ci ou +225 07 12 34 56 78");
        assertTrue(r.contains("[EMAIL]"));
        assertTrue(r.contains("[PHONE]"));
    }
    @Test
    void chunkProducesOverlap() {
        String t = "x".repeat(5000);
        var chunks = IngestionService.chunk(t, 512, 50);
        assertTrue(chunks.size() > 1);
    }
}

