package ci.aiplatform.payroll.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix = "payroll.ci")
public class PayrollProperties {
    private BigDecimal tauxCnpsSalarial;
    private BigDecimal tauxCnpsPatronal;
    private BigDecimal plafondCnps;
    private BigDecimal smic;
    private BigDecimal cmuFixe;
    private BigDecimal fpcTaux;
    private BigDecimal taxeApprentissage;
}

