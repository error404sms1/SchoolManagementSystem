package SchoolManagementSystem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository
        extends JpaRepository<StudentProfile, String> {
}
