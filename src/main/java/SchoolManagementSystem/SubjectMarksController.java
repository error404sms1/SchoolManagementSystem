package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subject-marks")
public class SubjectMarksController {

    private final SubjectMarksService service;

    public SubjectMarksController(
            SubjectMarksService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<SubjectMarks> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SubjectMarks getOne(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public SubjectMarks create(
            @RequestBody SubjectMarks value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public SubjectMarks update(
            @PathVariable Long id,
            @RequestBody SubjectMarks value
    ) {

        value.setId(id);

        return service.update(
                id,
                value
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
