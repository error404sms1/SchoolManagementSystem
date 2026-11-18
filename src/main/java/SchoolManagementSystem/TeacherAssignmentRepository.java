package SchoolManagementSystem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherAssignmentRepository
        extends JpaRepository<TeacherAssignment, String> {
}
