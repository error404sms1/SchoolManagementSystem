package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignment-marks")
public class AssignmentMarksController {

    private final AssignmentMarksService service;

    public AssignmentMarksController(
            AssignmentMarksService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<AssignmentMarks> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AssignmentMarks getOne(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public AssignmentMarks create(
            @RequestBody AssignmentMarks value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public AssignmentMarks update(
            @PathVariable Long id,
            @RequestBody AssignmentMarks value
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
