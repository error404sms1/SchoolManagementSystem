package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-profiles")
public class StudentProfileController {

    private final StudentProfileService service;

    public StudentProfileController(
            StudentProfileService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<StudentProfile> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public StudentProfile getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public StudentProfile create(
            @RequestBody StudentProfile value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public StudentProfile update(
            @PathVariable String id,
            @RequestBody StudentProfile value
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
