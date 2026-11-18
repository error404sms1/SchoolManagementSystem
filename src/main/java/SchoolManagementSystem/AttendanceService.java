package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(
            AttendanceRepository repository
    ) {
        this.repository = repository;
    }

    public List<Attendance> findAll() {
        return repository.findAll();
    }

    public Attendance findById(
            Long id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Attendance not found: "
                                        + id
                        )
                );
    }

    public Attendance save(
            Attendance value
    ) {
        return repository.save(value);
    }

    public Attendance update(
            Long id,
            Attendance value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Attendance not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Attendance not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
