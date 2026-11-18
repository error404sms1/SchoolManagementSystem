package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NonAcademicService {

    private final NonAcademicRepository repository;

    public NonAcademicService(
            NonAcademicRepository repository
    ) {
        this.repository = repository;
    }

    public List<NonAcademic> findAll() {
        return repository.findAll();
    }

    public NonAcademic findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "NonAcademic not found: "
                                        + id
                        )
                );
    }

    public NonAcademic save(
            NonAcademic value
    ) {
        return repository.save(value);
    }

    public NonAcademic update(
            String id,
            NonAcademic value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "NonAcademic not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "NonAcademic not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
