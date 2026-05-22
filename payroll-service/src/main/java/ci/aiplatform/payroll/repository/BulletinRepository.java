package ci.aiplatform.payroll.repository;

import ci.aiplatform.payroll.domain.BulletinDePaie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BulletinRepository extends JpaRepository<BulletinDePaie, UUID> {
    List<BulletinDePaie> findAllByPeriod(String period);
}

