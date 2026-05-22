package ci.aiplatform.payroll.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "bulletins", indexes = @Index(name = "idx_bul_emp_period", columnList = "employeeId,period"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BulletinDePaie {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID employeeId;

    @Column(nullable = false) private String period; // "2026-04"

    // Rémunération
    private BigDecimal salaireBrutBase;
    @Builder.Default private BigDecimal primes = BigDecimal.ZERO;
    @Builder.Default private BigDecimal heuresSup = BigDecimal.ZERO;
    @Builder.Default private BigDecimal avantagesNature = BigDecimal.ZERO;
    private BigDecimal salaireBrutTotal;

    // Retenues salariales
    private BigDecimal cnpsSalarial;
    private BigDecimal its;            // Impôt sur Traitements et Salaires
    private BigDecimal cmuSalarial;
    private BigDecimal totalRetenues;

    // Charges patronales
    private BigDecimal cnpsPatronal;
    private BigDecimal fpc;            // Formation Pro Continue
    private BigDecimal taxeApprentissage;
    private BigDecimal cmuPatronale;

    // Nets
    private BigDecimal netImposable;
    private BigDecimal netAPayer;
    private BigDecimal coutTotalEmployeur;

    @Enumerated(EnumType.STRING) @Builder.Default
    private BulletinStatus status = BulletinStatus.DRAFT;
}

