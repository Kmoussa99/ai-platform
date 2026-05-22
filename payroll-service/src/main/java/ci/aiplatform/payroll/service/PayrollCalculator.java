package ci.aiplatform.payroll.service;

import ci.aiplatform.payroll.domain.*;
import ci.aiplatform.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Calcul de paie selon le droit ivoirien :
 * - CNPS salarial 3.2% / patronal 14.9% sur plafond 1 647 315 FCFA
 * - CMU 1000 FCFA salarial + 1000 FCFA patronal
 * - ITS : barème progressif DGI 2024
 * - FPC 1.2% + Taxe apprentissage 0.4% (sur brut)
 */
@Service
@RequiredArgsConstructor
public class PayrollCalculator {

    private final PayrollProperties props;
    private final EmployeeRepository employeeRepo;
    private final BulletinRepository bulletinRepo;

    /** Barème ITS 2024 (mensuel, FCFA) — tranches progressives. */
    private static final List<double[]> ITS_TRANCHES = List.of(
        new double[]{ 75_000,    0.00 },
        new double[]{ 240_000,   0.16 },
        new double[]{ 800_000,   0.21 },
        new double[]{ 2_400_000, 0.24 },
        new double[]{ 8_000_000, 0.28 },
        new double[]{ Double.MAX_VALUE, 0.32 }
    );

    @Transactional
    public BulletinDePaie calculate(UUID employeeId, String period,
                                     BigDecimal primes, BigDecimal heuresSup,
                                     BigDecimal avantages) {
        Employee e = employeeRepo.findById(employeeId).orElseThrow();
        return compute(e, period,
            n(primes), n(heuresSup), n(avantages));
    }

    public BulletinDePaie compute(Employee e, String period,
                                   BigDecimal primes, BigDecimal heuresSup, BigDecimal avantages) {
        BigDecimal brutBase = n(e.getSalaireBrutBase());
        BigDecimal brutTotal = brutBase.add(primes).add(heuresSup).add(avantages);

        BigDecimal assietteCnps = brutTotal.min(props.getPlafondCnps());

        BigDecimal cnpsSal = assietteCnps.multiply(props.getTauxCnpsSalarial());
        BigDecimal cnpsPat = assietteCnps.multiply(props.getTauxCnpsPatronal());
        BigDecimal cmuSal = props.getCmuFixe();
        BigDecimal cmuPat = props.getCmuFixe();

        BigDecimal netImposable = brutTotal.subtract(cnpsSal).subtract(cmuSal);
        BigDecimal its = computeITS(netImposable);

        BigDecimal totalRetenues = cnpsSal.add(its).add(cmuSal);
        BigDecimal netAPayer = brutTotal.subtract(totalRetenues);

        BigDecimal fpc = brutTotal.multiply(props.getFpcTaux());
        BigDecimal apprent = brutTotal.multiply(props.getTaxeApprentissage());
        BigDecimal coutTotal = brutTotal.add(cnpsPat).add(cmuPat).add(fpc).add(apprent);

        BulletinDePaie b = BulletinDePaie.builder()
            .employeeId(e.getId()).period(period)
            .salaireBrutBase(round(brutBase))
            .primes(round(primes)).heuresSup(round(heuresSup)).avantagesNature(round(avantages))
            .salaireBrutTotal(round(brutTotal))
            .cnpsSalarial(round(cnpsSal)).its(round(its)).cmuSalarial(round(cmuSal))
            .totalRetenues(round(totalRetenues))
            .cnpsPatronal(round(cnpsPat)).fpc(round(fpc))
            .taxeApprentissage(round(apprent)).cmuPatronale(round(cmuPat))
            .netImposable(round(netImposable)).netAPayer(round(netAPayer))
            .coutTotalEmployeur(round(coutTotal))
            .status(BulletinStatus.DRAFT)
            .build();
        return bulletinRepo.save(b);
    }

    public BigDecimal computeITS(BigDecimal netImposable) {
        double remaining = netImposable.doubleValue();
        double total = 0;
        double prev = 0;
        for (double[] t : ITS_TRANCHES) {
            double seuil = t[0], taux = t[1];
            if (remaining <= 0) break;
            double tranche = Math.min(seuil - prev, remaining);
            total += tranche * taux;
            remaining -= tranche;
            prev = seuil;
        }
        return BigDecimal.valueOf(total);
    }

    private BigDecimal n(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal round(BigDecimal v) { return v.setScale(0, RoundingMode.HALF_UP); }
}

