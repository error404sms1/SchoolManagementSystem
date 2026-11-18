package SchoolManagementSystem;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdmissionGrade1Service {

    private final AdmissionGrade1Repository repository;

    public AdmissionGrade1Service(
            AdmissionGrade1Repository repository
    ) {
        this.repository = repository;
    }

    public List<AdmissionGrade1> findAll() {
        return repository.findAll();
    }

    public AdmissionGrade1 findById(
            String id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Grade 1 admission not found: "
                                        + id
                        )
                );
    }

    public AdmissionGrade1 save(
            AdmissionGrade1 admission
    ) {

        if (admission.getAssignDate() == null) {
            admission.setAssignDate(
                    LocalDate.now()
            );
        }

        if (!admission.isAgeEligible()) {
            throw new IllegalArgumentException(
                    "Student must be at least 5 years old "
                            + "on the assignment date"
            );
        }

        admission.calculateMarks();

        return repository.save(admission);
    }

    public AdmissionGrade1 update(
            String id,
            AdmissionGrade1 admission
    ) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Grade 1 admission not found: "
                            + id
            );
        }

        admission.setAdmissionId(id);

        return save(admission);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Grade 1 admission not found: "
                            + id
            );
        }

        repository.deleteById(id);
    }
}
