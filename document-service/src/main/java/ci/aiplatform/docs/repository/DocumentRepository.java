package ci.aiplatform.docs.repository;

import ci.aiplatform.docs.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {}

