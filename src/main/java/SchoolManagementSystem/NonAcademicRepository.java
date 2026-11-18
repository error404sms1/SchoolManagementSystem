package SchoolManagementSystem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NonAcademicRepository
        extends JpaRepository<NonAcademic, String> {
}
