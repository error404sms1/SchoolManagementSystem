package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admissions/al")
public class AdmissionALController {

    private final AdmissionALService service;

    public AdmissionALController(
            AdmissionALService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<AdmissionAL> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AdmissionAL getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public AdmissionAL create(
            @RequestBody AdmissionAL value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public AdmissionAL update(
            @PathVariable String id,
            @RequestBody AdmissionAL value
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

        AdmissionAL admission =
                service.findById(id);

        return Map.of(
                "stream",
                admission.getStreamName(),
                "eligible",
                admission.isEligible()
        );
    }
}
