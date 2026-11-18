package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService service;

    public TeacherController(
            TeacherService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<Teacher> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Teacher getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public Teacher create(
            @RequestBody Teacher value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public Teacher update(
            @PathVariable String id,
            @RequestBody Teacher value
    ) {

        value.setTeacherId(id);

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
