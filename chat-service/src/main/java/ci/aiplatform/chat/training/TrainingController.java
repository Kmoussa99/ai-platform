package ci.aiplatform.chat.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingExampleRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/examples")
    public List<TrainingExample> list(@RequestParam(required = false) TrainingStatus status) {
        return status == null ? repo.findAll() : repo.findAllByStatus(status);
    }

    public record ReviewReq(TrainingStatus status, String reviewedBy, Float qualityScore) {}

    @PutMapping("/examples/{id}/review")
    public TrainingExample review(@PathVariable UUID id, @RequestBody ReviewReq r) {
        TrainingExample e = repo.findById(id).orElseThrow();
        e.setStatus(r.status()); e.setReviewedBy(r.reviewedBy()); e.setQualityScore(r.qualityScore());
        return repo.save(e);
    }

    /** Export JSONL Alpaca / DPO format. */
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public String export(@RequestParam(defaultValue = "alpaca") String format) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (TrainingExample e : repo.findAllByStatus(TrainingStatus.APPROVED)) {
            Map<String, Object> row;
            if ("dpo".equalsIgnoreCase(format)) {
                row = Map.of("prompt", e.getInstruction(),
                    "chosen", e.getResponseChosen(),
                    "rejected", e.getResponseRejected() == null ? "" : e.getResponseRejected());
            } else {
                row = Map.of("instruction", e.getInstruction(),
                    "input", "", "output", e.getResponseChosen());
            }
            sb.append(mapper.writeValueAsString(row)).append("\n");
        }
        return sb.toString();
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        Map<String, Long> out = new HashMap<>();
        for (TrainingStatus s : TrainingStatus.values()) {
            out.put(s.name(), (long) repo.findAllByStatus(s).size());
        }
        return out;
    }
}

