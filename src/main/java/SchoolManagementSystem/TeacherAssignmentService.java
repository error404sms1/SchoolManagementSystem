package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository repository;

    public TeacherAssignmentService(
            TeacherAssignmentRepository repository
    ) {
        this.repository = repository;
    }

    public List<TeacherAssignment> findAll() {
        return repository.findAll();
    }

    public TeacherAssignment findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "TeacherAssignment not found: "
                                        + id
                        )
                );
    }

    public TeacherAssignment save(
            TeacherAssignment value
    ) {
        return repository.save(value);
    }

    public TeacherAssignment update(
            String id,
            TeacherAssignment value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "TeacherAssignment not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "TeacherAssignment not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
