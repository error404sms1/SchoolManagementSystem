package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(
            StudentRepository repository
    ) {
        this.repository = repository;
    }

    public List<Student> findAll() {
        return repository.findAll();
    }

    public Student findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Student not found: "
                                        + id
                        )
                );
    }

    public Student save(
            Student value
    ) {
        return repository.save(value);
    }

    public Student update(
            String id,
            Student value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Student not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Student not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
