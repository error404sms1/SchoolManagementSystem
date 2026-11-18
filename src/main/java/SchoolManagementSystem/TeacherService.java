package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository repository;

    public TeacherService(
            TeacherRepository repository
    ) {
        this.repository = repository;
    }

    public List<Teacher> findAll() {
        return repository.findAll();
    }

    public Teacher findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Teacher not found: "
                                        + id
                        )
                );
    }

    public Teacher save(
            Teacher value
    ) {
        return repository.save(value);
    }

    public Teacher update(
            String id,
            Teacher value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Teacher not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Teacher not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
