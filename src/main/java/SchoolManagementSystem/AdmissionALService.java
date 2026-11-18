package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdmissionALService {

    private final AdmissionALRepository repository;

    public AdmissionALService(AdmissionALRepository repository) {
        this.repository = repository;
    }

    public List<AdmissionAL> findAll() {
        return repository.findAll();
    }

    public AdmissionAL findById(String id) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "A/L admission not found: " + id
                        )
                );
    }

    public AdmissionAL save(AdmissionAL value) {

        if (value.getStream() < 1 || value.getStream() > 4) {
            throw new IllegalArgumentException(
                    "Stream must be 1, 2, 3, or 4"
            );
        }

        return repository.save(value);
    }

    public AdmissionAL update(
            String id,
            AdmissionAL value
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "A/L admission not found: " + id
            );
        }

        value.setAdmissionId(id);

        return save(value);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "A/L admission not found: " + id
            );
        }

        repository.deleteById(id);
    }
}
