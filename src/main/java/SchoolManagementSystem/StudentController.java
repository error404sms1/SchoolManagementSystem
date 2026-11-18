package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(
            StudentService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<Student> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Student getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public Student create(
            @RequestBody Student value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public Student update(
            @PathVariable String id,
            @RequestBody Student value
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
