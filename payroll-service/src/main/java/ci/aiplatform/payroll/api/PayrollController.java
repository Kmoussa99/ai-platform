package ci.aiplatform.payroll.api;

import ci.aiplatform.payroll.domain.*;
import ci.aiplatform.payroll.repository.*;
import ci.aiplatform.payroll.service.PayrollCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollCalculator calculator;
    private final EmployeeRepository employees;
    private final BulletinRepository bulletins;

    public record CalcReq(BigDecimal primes, BigDecimal heuresSup, BigDecimal avantages) {}

    @PostMapping("/calculate/{employeeId}/{period}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    public BulletinDePaie calculate(@PathVariable UUID employeeId,
                                     @PathVariable String period,
                                     @RequestBody(required = false) CalcReq req) {
        CalcReq r = req == null ? new CalcReq(null, null, null) : req;
        return calculator.calculate(employeeId, period, r.primes(), r.heuresSup(), r.avantages());
    }

    @PostMapping("/calculate-batch/{period}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    public List<BulletinDePaie> calculateBatch(@PathVariable String period) {
        return employees.findAll().stream()
            .map(e -> calculator.calculate(e.getId(), period, null, null, null))
            .toList();
    }

    @PostMapping("/validate/{bulletinId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    public BulletinDePaie validate(@PathVariable UUID bulletinId) {
        BulletinDePaie b = bulletins.findById(bulletinId).orElseThrow();
        b.setStatus(BulletinStatus.VALIDATED);
        return bulletins.save(b);
    }

    @GetMapping(value = "/export/dsf/{period}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String exportDsf(@PathVariable String period) {
        StringBuilder sb = new StringBuilder("MATRICULE;NOM;BRUT;CNPS;ITS;NET\n");
        for (BulletinDePaie b : bulletins.findAllByPeriod(period)) {
            Employee e = employees.findById(b.getEmployeeId()).orElseThrow();
            sb.append(e.getMatricule()).append(";")
              .append(e.getNom()).append(" ").append(e.getPrenoms()).append(";")
              .append(b.getSalaireBrutTotal()).append(";")
              .append(b.getCnpsSalarial()).append(";")
              .append(b.getIts()).append(";")
              .append(b.getNetAPayer()).append("\n");
        }
        return sb.toString();
    }

    @GetMapping(value = "/export/virements/{period}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String exportVirements(@PathVariable String period) {
        StringBuilder sb = new StringBuilder("BANQUE;BENEFICIAIRE;MONTANT\n");
        for (BulletinDePaie b : bulletins.findAllByPeriod(period)) {
            Employee e = employees.findById(b.getEmployeeId()).orElseThrow();
            sb.append("SGCI;").append(e.getNom()).append(" ").append(e.getPrenoms())
              .append(";").append(b.getNetAPayer()).append("\n");
        }
        return sb.toString();
    }

    @GetMapping(value = "/export/journal/{period}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String exportJournal(@PathVariable String period) {
        StringBuilder sb = new StringBuilder("COMPTE;LIBELLE;DEBIT;CREDIT\n");
        for (BulletinDePaie b : bulletins.findAllByPeriod(period)) {
            sb.append("661;Salaires bruts;").append(b.getSalaireBrutTotal()).append(";0\n");
            sb.append("431;CNPS;0;").append(b.getCnpsSalarial().add(b.getCnpsPatronal())).append("\n");
            sb.append("447;ITS;0;").append(b.getIts()).append("\n");
            sb.append("421;Net à payer;0;").append(b.getNetAPayer()).append("\n");
        }
        return sb.toString();
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ACCOUNTANT')")
    public BulletinDePaie simulate(@RequestBody Employee transient_) {
        return calculator.compute(transient_, "SIMULATION",
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}

