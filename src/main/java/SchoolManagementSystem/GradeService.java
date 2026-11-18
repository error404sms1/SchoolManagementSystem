package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeService {

    private final GradeRepository repository;

    public GradeService(
            GradeRepository repository
    ) {
        this.repository = repository;
    }

    public List<Grade> findAll() {
        return repository.findAll();
    }

    public Grade findById(String id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Grade not found: "
                                        + id
                        )
                );
    }

    public Grade save(Grade value) {
        return repository.save(value);
    }

    public Grade update(
            String id,
            Grade value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Grade not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Grade not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
