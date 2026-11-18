package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentMarksService {

    private final AssignmentMarksRepository repository;

    public AssignmentMarksService(
            AssignmentMarksRepository repository
    ) {
        this.repository = repository;
    }

    public List<AssignmentMarks> findAll() {
        return repository.findAll();
    }

    public AssignmentMarks findById(
            Long id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "AssignmentMarks not found: "
                                        + id
                        )
                );
    }

    public AssignmentMarks save(
            AssignmentMarks value
    ) {
        return repository.save(value);
    }

    public AssignmentMarks update(
            Long id,
            AssignmentMarks value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "AssignmentMarks not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "AssignmentMarks not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
