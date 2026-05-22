package ci.aiplatform.payroll.api;

import ci.aiplatform.payroll.domain.Employee;
import ci.aiplatform.payroll.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeRepository repo;

    @GetMapping public List<Employee> list() { return repo.findAll(); }
    @PostMapping public Employee create(@RequestBody Employee e) { return repo.save(e); }
    @GetMapping("/{id}") public Employee get(@PathVariable UUID id) { return repo.findById(id).orElseThrow(); }
    @PutMapping("/{id}") public Employee update(@PathVariable UUID id, @RequestBody Employee e) { e.setId(id); return repo.save(e); }
    @DeleteMapping("/{id}") public void delete(@PathVariable UUID id) { repo.deleteById(id); }
}

