package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/non-academic")
public class NonAcademicController {

    private final NonAcademicService service;

    public NonAcademicController(
            NonAcademicService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<NonAcademic> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public NonAcademic getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public NonAcademic create(
            @RequestBody NonAcademic value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public NonAcademic update(
            @PathVariable String id,
            @RequestBody NonAcademic value
    ) {

        value.setEmployeeId(id);

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
