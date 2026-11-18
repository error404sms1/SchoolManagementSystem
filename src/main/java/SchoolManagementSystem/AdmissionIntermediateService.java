package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdmissionIntermediateService {

    private final AdmissionIntermediateRepository repository;

    public AdmissionIntermediateService(
            AdmissionIntermediateRepository repository
    ) {
        this.repository = repository;
    }

    public List<AdmissionIntermediate> findAll() {
        return repository.findAll();
    }

    public AdmissionIntermediate findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Intermediate admission not found: "
                                        + id
                        )
                );
    }

    public AdmissionIntermediate save(
            AdmissionIntermediate value
    ) {
        return repository.save(value);
    }

    public AdmissionIntermediate update(
            String id,
            AdmissionIntermediate value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Intermediate admission not found: "
                            + id
            );
        }

        value.setAdmissionId(id);

        return repository.save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Intermediate admission not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
