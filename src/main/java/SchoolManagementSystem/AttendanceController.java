package SchoolManagementSystem;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(
            AttendanceService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<Attendance> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Attendance getOne(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PostMapping
    public Attendance create(
            @RequestBody Attendance value
    ) {
        return service.save(value);
    }

    @PutMapping("/{id}")
    public Attendance update(
            @PathVariable Long id,
            @RequestBody Attendance value
    ) {

        value.setAttendanceId(id);

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
