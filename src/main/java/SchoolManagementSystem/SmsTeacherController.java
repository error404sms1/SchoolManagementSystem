package SchoolManagementSystem;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class SmsTeacherController {
    private final JdbcTemplate jdbc;
    private final SmsAccessService access;
    public SmsTeacherController(JdbcTemplate jdbc, SmsAccessService access) {
        this.jdbc = jdbc; this.access = access;
    }

    @GetMapping("/my-scopes")
    public List<Map<String,Object>> scopes() {
        if (access.admin()) return jdbc.queryForList("SELECT * FROM TEACHER_SCOPE ORDER BY teacher_id,grade_level,subject_id");
        return jdbc.queryForList("SELECT * FROM TEACHER_SCOPE WHERE teacher_id=? ORDER BY grade_level,subject_id", access.teacherCode());
    }

    @GetMapping("/students")
    public List<Map<String,Object>> myStudents(@RequestParam int grade, @RequestParam String subjectId) {
        access.requireScope(grade, subjectId);
        return jdbc.queryForList("SELECT e.student_id,e.grade_number,e.seat_type,s.studentname,s.birthday,s.studentaddress " +
            "FROM sms_student_enrollment e JOIN STUDENT s ON s.Studentid=e.student_id " +
            "WHERE e.grade_number=? AND e.active=TRUE ORDER BY s.studentname", grade);
    }

    @PostMapping("/attendance")
    public Map<String,String> attendance(@RequestBody SmsDtos.Attendance value) {
        if (value.date() == null) throw new IllegalArgumentException("Date required");
        int grade = access.requireStudentSubject(value.studentId(), value.subjectId());
        jdbc.update("INSERT INTO sms_attendance(student_id,attendance_date,grade_number,subject_id,present,marked_by) " +
            "VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE present=VALUES(present),marked_by=VALUES(marked_by),grade_number=VALUES(grade_number)",
            value.studentId(), value.date(), grade, value.subjectId(), value.present(), access.username());
        return Map.of("message", "Attendance saved");
    }

    @GetMapping("/attendance")
    public List<Map<String,Object>> attendanceList(@RequestParam int grade, @RequestParam String subjectId) {
        access.requireScope(grade, subjectId);
        return jdbc.queryForList("SELECT student_id,attendance_date,present,marked_by FROM sms_attendance " +
            "WHERE grade_number=? AND subject_id=? ORDER BY attendance_date DESC,student_id", grade,subjectId);
    }

    @PostMapping("/marks")
    public Map<String,String> marks(@RequestBody SmsDtos.Mark value) {
        if (value.mark() == null || value.mark().compareTo(BigDecimal.ZERO) < 0 ||
                value.mark().compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Marks must be 0 to 100");
        if (value.termName() == null || value.termName().isBlank() || value.termName().length()>60)
            throw new IllegalArgumentException("Term name required (maximum 60 characters)");
        int grade = access.requireStudentSubject(value.studentId(),value.subjectId());
        jdbc.update("INSERT INTO sms_subject_marks(student_id,subject_id,grade_number,mark,term_name,entered_by) " +
            "VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE mark=VALUES(mark),grade_number=VALUES(grade_number)," +
            "entered_by=VALUES(entered_by)", value.studentId(), value.subjectId(),grade,
            value.mark(),value.termName(),access.username());
        return Map.of("message", "Subject marks saved");
    }

    @GetMapping("/marks")
    public List<Map<String,Object>> studentMarks(@RequestParam String studentId,
            @RequestParam String subjectId) {
        access.requireStudentSubject(studentId, subjectId);
        return jdbc.queryForList("SELECT student_id,subject_id,grade_number,term_name,mark,entered_by,updated_at " +
            "FROM sms_subject_marks WHERE student_id=? AND subject_id=? ORDER BY term_name",
            studentId,subjectId);
    }

    @PostMapping("/profile-notes")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> addProfileNote(@RequestBody SmsDtos.ProfileNote value) {
        int grade = access.requireStudentSubject(value.studentId(),value.subjectId());
        if ((value.achievements() == null || value.achievements().isBlank()) &&
            (value.behaviour() == null || value.behaviour().isBlank()))
            throw new IllegalArgumentException("Add achievements or behaviour information");
        jdbc.update("INSERT INTO sms_profile_note(student_id,grade_number,subject_id,achievements,behaviour,entered_by) " +
            "VALUES(?,?,?,?,?,?)", value.studentId(),grade,value.subjectId(),
            value.achievements(), value.behaviour(),access.username());
        return Map.of("message", "Student profile note saved");
    }

    @GetMapping("/profile/{studentId}")
    public Map<String,Object> profile(@PathVariable String studentId) {
        access.requireStudentAnyScope(studentId);
        if (access.admin()) {
            return Map.of("student", student(studentId),
                "notes", jdbc.queryForList("SELECT * FROM sms_profile_note WHERE student_id=? ORDER BY created_at DESC",studentId),
                "marks", jdbc.queryForList("SELECT * FROM sms_subject_marks WHERE student_id=? ORDER BY subject_id,term_name",studentId));
        }
        String code = access.teacherCode();
        return Map.of("student",student(studentId),
            "notes",jdbc.queryForList("SELECT n.* FROM sms_profile_note n JOIN TEACHER_SCOPE sc ON " +
                "sc.grade_level=n.grade_number AND sc.subject_id=n.subject_id " +
                "WHERE n.student_id=? AND sc.teacher_id=? ORDER BY n.created_at DESC",studentId,code),
            "marks",jdbc.queryForList("SELECT m.* FROM sms_subject_marks m JOIN TEACHER_SCOPE sc ON " +
                "sc.grade_level=m.grade_number AND sc.subject_id=m.subject_id " +
                "WHERE m.student_id=? AND sc.teacher_id=? ORDER BY m.subject_id,m.term_name",studentId,code));
    }

    private Map<String,Object> student(String studentId) {
        List<Map<String,Object>> rows = jdbc.queryForList(
            "SELECT * FROM STUDENT WHERE Studentid=?",studentId);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        return rows.get(0);
    }
}
