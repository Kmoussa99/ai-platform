package ci.aiplatform.payroll.service;

import ci.aiplatform.payroll.domain.*;
import ci.aiplatform.payroll.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PayrollCalculatorTest {

    PayrollProperties props;
    PayrollCalculator calc;
    BulletinRepository bulletinRepo;

    @BeforeEach
    void setup() {
        props = new PayrollProperties();
        props.setTauxCnpsSalarial(new BigDecimal("0.032"));
        props.setTauxCnpsPatronal(new BigDecimal("0.149"));
        props.setPlafondCnps(new BigDecimal("1647315"));
        props.setSmic(new BigDecimal("60000"));
        props.setCmuFixe(new BigDecimal("1000"));
        props.setFpcTaux(new BigDecimal("0.012"));
        props.setTaxeApprentissage(new BigDecimal("0.004"));
        bulletinRepo = Mockito.mock(BulletinRepository.class);
        Mockito.when(bulletinRepo.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        calc = new PayrollCalculator(props, Mockito.mock(EmployeeRepository.class), bulletinRepo);
    }

    @Test
    void salaireMoyen500k() {
        Employee e = Employee.builder().id(UUID.randomUUID())
            .matricule("EMP001").nom("Kone").prenoms("Moussa")
            .salaireBrutBase(new BigDecimal("500000")).build();
        BulletinDePaie b = calc.compute(e, "2026-04",
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(0, b.getCnpsSalarial().compareTo(new BigDecimal("16000")));
        assertTrue(b.getNetAPayer().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(b.getCoutTotalEmployeur().compareTo(b.getSalaireBrutTotal()) > 0);
    }

    @Test
    void plafondCnpsApplique() {
        Employee e = Employee.builder().id(UUID.randomUUID())
            .salaireBrutBase(new BigDecimal("3000000")).build();
        BulletinDePaie b = calc.compute(e, "2026-04",
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        // CNPS calculé sur le plafond, pas sur 3M
        assertEquals(0, b.getCnpsSalarial()
            .compareTo(new BigDecimal("1647315").multiply(new BigDecimal("0.032"))
                .setScale(0, java.math.RoundingMode.HALF_UP)));
    }

    @Test
    void itsZeroSousPremierTranche() {
        BigDecimal its = calc.computeITS(new BigDecimal("50000"));
        assertEquals(0, its.compareTo(BigDecimal.ZERO));
    }

    @Test
    void itsProgressif() {
        BigDecimal its = calc.computeITS(new BigDecimal("300000"));
        assertTrue(its.compareTo(BigDecimal.ZERO) > 0);
    }
}

