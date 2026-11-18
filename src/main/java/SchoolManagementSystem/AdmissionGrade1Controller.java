package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admissions/grade1")
public class AdmissionGrade1Controller {

    private final AdmissionGrade1Service service;

    public AdmissionGrade1Controller(
            AdmissionGrade1Service service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<AdmissionGrade1> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AdmissionGrade1 getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public AdmissionGrade1 create(
            @RequestBody AdmissionGrade1 value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public AdmissionGrade1 update(
            @PathVariable String id,
            @RequestBody AdmissionGrade1 value
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

    @GetMapping("/{id}/result")
    public Map<String, Object> result(
            @PathVariable String id
    ) {

        AdmissionGrade1 admission =
                service.findById(id);

        return Map.of(
                "ageEligible",
                admission.isAgeEligible(),
                "marks",
                admission.getMarks()
        );
    }
}
