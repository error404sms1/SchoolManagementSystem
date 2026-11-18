package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-assignments")
public class TeacherAssignmentController {

    private final TeacherAssignmentService service;

    public TeacherAssignmentController(
            TeacherAssignmentService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<TeacherAssignment> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TeacherAssignment getOne(
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public TeacherAssignment create(
            @RequestBody TeacherAssignment value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public TeacherAssignment update(
            @PathVariable String id,
            @RequestBody TeacherAssignment value
    ) {

        value.setAssiId(id);

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
