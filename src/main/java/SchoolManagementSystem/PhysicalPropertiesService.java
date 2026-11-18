package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhysicalPropertiesService {

    private final PhysicalPropertiesRepository repository;

    public PhysicalPropertiesService(
            PhysicalPropertiesRepository repository
    ) {
        this.repository = repository;
    }

    public List<PhysicalProperties> findAll() {
        return repository.findAll();
    }

    public PhysicalProperties findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "PhysicalProperties not found: "
                                        + id
                        )
                );
    }

    public PhysicalProperties save(
            PhysicalProperties value
    ) {
        return repository.save(value);
    }

    public PhysicalProperties update(
            String id,
            PhysicalProperties value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "PhysicalProperties not found: "
                            + id
            );
        }

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "PhysicalProperties not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
