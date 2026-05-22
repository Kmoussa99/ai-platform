package ci.aiplatform.docs.api;

import ci.aiplatform.docs.domain.Document;
import ci.aiplatform.docs.repository.DocumentRepository;
import ci.aiplatform.docs.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IngestionService ingestion;
    private final DocumentRepository repo;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Document upload(@AuthenticationPrincipal Jwt jwt,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(required = false) String collection,
                           @RequestParam(required = false) String accessLevel,
                           @RequestParam(required = false) List<String> tags) throws Exception {
        return ingestion.upload(file, collection, accessLevel, tags, jwt.getSubject());
    }

    @GetMapping
    public List<Document> list() { return repo.findAll(); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reindex")
    public Document reindex(@PathVariable UUID id) {
        Document d = repo.findById(id).orElseThrow();
        d.setStatus("PENDING");
        return repo.save(d);
    }
}

