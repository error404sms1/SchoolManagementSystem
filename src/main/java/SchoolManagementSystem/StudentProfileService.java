package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentProfileService {

    private final StudentProfileRepository repository;

    public StudentProfileService(
            StudentProfileRepository repository
    ) {
        this.repository = repository;
    }

    public List<StudentProfile> findAll() {
        return repository.findAll();
    }

    public StudentProfile findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "StudentProfile not found: "
                                        + id
                        )
                );
    }

    public StudentProfile save(
            StudentProfile value
    ) {
        return repository.save(value);
    }

    public StudentProfile update(
            String id,
            StudentProfile value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "StudentProfile not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "StudentProfile not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
