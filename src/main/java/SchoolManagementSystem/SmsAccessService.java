package SchoolManagementSystem;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class SmsAccessService {
    private final JdbcTemplate jdbc;
    public SmsAccessService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Authentication authentication() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        return a;
    }
    public String username() { return authentication().getName(); }
    public boolean admin() {
        return authentication().getAuthorities().stream()
            .anyMatch(x -> "ROLE_ADMIN".equals(x.getAuthority()));
    }
    public String teacherCode() {
        if (admin()) return "ADMIN";
        List<String> codes = jdbc.query(
            "SELECT teacher_id FROM APP_USER WHERE username=? AND role='TEACHER' AND enabled=TRUE",
            (rs,i) -> rs.getString(1), username());
        if (codes.isEmpty()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a teacher account");
        return codes.get(0);
    }
    public void requireScope(int grade, String subjectId) {
        if (admin()) return;
        if (subjectId == null || subjectId.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject ID required");
        Integer n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM TEACHER_SCOPE WHERE teacher_id=? AND grade_level=? AND subject_id=?",
            Integer.class, teacherCode(), grade, subjectId);
        if (n == null || n == 0) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
            "Teacher is not assigned to this grade and subject");
    }
    public int gradeForStudent(String studentId) {
        List<Integer> result = jdbc.query(
            "SELECT grade_number FROM sms_student_enrollment WHERE student_id=? AND active=TRUE",
            (rs, i) -> rs.getInt(1), studentId);
        if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Student is not enrolled in the grade register");
        return result.get(0);
    }
    public int requireStudentSubject(String studentId, String subjectId) {
        int grade = gradeForStudent(studentId);
        requireScope(grade, subjectId);
        return grade;
    }
    public int requireStudentAnyScope(String studentId) {
        // Administrators must be able to search existing STUDENT records by ID
        // even before that student has been enrolled in the new grade register.
        if (admin()) return 0;
        int grade = gradeForStudent(studentId);
        {
            Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM TEACHER_SCOPE WHERE teacher_id=? AND grade_level=?",
                Integer.class, teacherCode(), grade);
            if (n == null || n == 0) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Teacher is not assigned to this student's grade");
        }
        return grade;
    }
}
