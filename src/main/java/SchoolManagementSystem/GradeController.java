package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService service;

    public GradeController(
            GradeService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<Grade> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Grade getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public Grade create(
            @RequestBody Grade value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public Grade update(
            @PathVariable String id,
            @RequestBody Grade value
    ) {

        value.setStudentId(id);

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
