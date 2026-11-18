package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectMarksService {

    private final SubjectMarksRepository repository;

    public SubjectMarksService(
            SubjectMarksRepository repository
    ) {
        this.repository = repository;
    }

    public List<SubjectMarks> findAll() {
        return repository.findAll();
    }

    public SubjectMarks findById(
            Long id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "SubjectMarks not found: "
                                        + id
                        )
                );
    }

    public SubjectMarks save(
            SubjectMarks value
    ) {
        return repository.save(value);
    }

    public SubjectMarks update(
            Long id,
            SubjectMarks value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "SubjectMarks not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "SubjectMarks not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
