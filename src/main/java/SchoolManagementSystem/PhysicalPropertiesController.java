package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PhysicalPropertiesController {

    private final PhysicalPropertiesService service;

    public PhysicalPropertiesController(
            PhysicalPropertiesService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<PhysicalProperties> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PhysicalProperties getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public PhysicalProperties create(
            @RequestBody PhysicalProperties value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public PhysicalProperties update(
            @PathVariable String id,
            @RequestBody PhysicalProperties value
    ) {

        value.setPropertyId(id);

        return service.update(
                id,
                value
        );
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
}
