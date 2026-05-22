package ci.aiplatform.payroll.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {
    @Id @GeneratedValue private UUID id;
    @Column(unique = true, nullable = false) private String matricule;
    private String nom;
    private String prenoms;
    private String poste;
    private String department;
    private java.math.BigDecimal salaireBrutBase;
    private String typeContrat;          // CDI / CDD / Stage
    private String regimeImposition;     // général / forfaitaire
    private String situationFamiliale;   // C, M, V, D
    private Integer nbEnfants;
    private String numeroCnps;
    private String numeroCmu;
    private LocalDate dateEmbauche;
}

