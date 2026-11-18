package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admissions/intermediate")
public class AdmissionIntermediateController {

    private final AdmissionIntermediateService service;

    public AdmissionIntermediateController(
            AdmissionIntermediateService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<AdmissionIntermediate> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AdmissionIntermediate getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public AdmissionIntermediate create(
            @RequestBody AdmissionIntermediate value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public AdmissionIntermediate update(
            @PathVariable String id,
            @RequestBody AdmissionIntermediate value
    ) {
        return service.update(id, value);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id
    ) {

        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}/grade/{grade}/available")
    public Map<String, Object> gradeAvailable(
            @PathVariable String id,
            @PathVariable int grade
    ) {

        boolean available =
                service.findById(id)
                        .isRegistrationAvailable(grade);

        return Map.of(
                "grade",
                grade,
                "available",
                available
        );
    }

    @GetMapping("/{id}/scholarship/{mark}/eligible")
    public Map<String, Object> scholarshipEligible(
            @PathVariable String id,
            @PathVariable int mark
    ) {

        boolean eligible =
                service.findById(id)
                        .checkSecondary(mark);

        return Map.of(
                "scholarshipMark",
                mark,
                "eligible",
                eligible
        );
    }
}
