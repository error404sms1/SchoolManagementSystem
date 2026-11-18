package SchoolManagementSystem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class SmsAdminController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final EntityManager entityManager;
    private final SmsTeacherController teachers;

    public SmsAdminController(JdbcTemplate jdbc, PasswordEncoder encoder,
            SmsTeacherController teachers, EntityManager entityManager) {
        this.jdbc=jdbc; this.encoder=encoder; this.teachers=teachers;
        this.entityManager=entityManager;
    }

    // Link an account to a teacher already registered by your existing
    // TeacherController. This uses JPA's entity identifier, so no attribute
    // names or Java class names inside your Teacher entity are changed.
    private void requireExistingTeacher(String teacherCode) {
        String entityName = entityManager.getMetamodel().entity(Teacher.class).getName();
        PersistenceUnitUtil identifiers = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        boolean found = entityManager.createQuery("SELECT t FROM " + entityName + " t", Teacher.class)
            .getResultStream().anyMatch(teacher -> {
                Object id = identifiers.getIdentifier(teacher);
                return id != null && teacherCode.equals(id.toString());
            });
        if (!found) throw new IllegalArgumentException(
            "Register this teacher in your EXISTING Teacher Management module first: " + teacherCode);
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> createUser(@RequestBody SmsDtos.CreateAccount value) {
        String username = SmsAuthController.validUsername(value.username());
        String pwd = encoder.encode(SmsAuthController.validPassword(value.password()));
        String role = value.role() == null ? "" : value.role().trim().toUpperCase(Locale.ROOT);
        if (!role.equals("ADMIN") && !role.equals("TEACHER"))
            throw new IllegalArgumentException("Role must be ADMIN or TEACHER");
        String code = null;
        if (role.equals("TEACHER")) {
            code = required(value.teacherCode(), "Teacher code");
            if (code.length()>50) throw new IllegalArgumentException("Teacher code too long");
            requireExistingTeacher(code);
        }
        jdbc.update("INSERT INTO APP_USER(username,password_hash,role,teacher_id) VALUES(?,?,?,?)",
            username,pwd,role,code);
        return Map.of("message", "Account registered; the teacher can now log in", "username",username);
    }

    @GetMapping("/accounts")
    public List<Map<String,Object>> accounts() {
        return jdbc.queryForList("SELECT user_id AS id,username,role,teacher_id AS teacherCode,enabled AS active,created_at " +
            "FROM APP_USER ORDER BY user_id");
    }

    @PutMapping("/accounts/{username}/active")
    public Map<String,String> accountActive(@PathVariable String username, @RequestParam boolean active) {
        if (active == false) {
            String role = jdbc.queryForObject("SELECT role FROM APP_USER WHERE username=?",
                String.class, username);
            if ("ADMIN".equals(role)) {
                Integer activeAdmins = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM APP_USER WHERE role='ADMIN' AND enabled=TRUE",Integer.class);
                if (activeAdmins != null && activeAdmins <= 1)
                    throw new IllegalArgumentException("Cannot disable the last active admin");
            }
        }
        int n=jdbc.update("UPDATE APP_USER SET enabled=? WHERE username=?",active,username);
        if(n==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found");
        return Map.of("message","Account updated");
    }

    @PutMapping("/accounts/{username}/password")
    public Map<String,String> resetPassword(@PathVariable String username, @RequestBody SmsDtos.Register value) {
        int n=jdbc.update("UPDATE APP_USER SET password_hash=? WHERE username=?",
            encoder.encode(SmsAuthController.validPassword(value.password())),username);
        if(n==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found");
        return Map.of("message","Password reset; user should log in again");
    }

    @PostMapping("/scopes")
    public Map<String,String> addScope(@RequestBody SmsDtos.Scope scope) {
        if(scope.gradeNumber()<1 || scope.gradeNumber()>13) throw new IllegalArgumentException("Grade must be 1–13");
        requireExistingTeacher(required(scope.teacherCode(),"Teacher code"));
        jdbc.update("INSERT INTO TEACHER_SCOPE(teacher_id,grade_level,subject_id) VALUES(?,?,?) " +
            "ON DUPLICATE KEY UPDATE subject_id=VALUES(subject_id)",
            required(scope.teacherCode(),"Teacher code"),scope.gradeNumber(),required(scope.subjectId(),"Subject ID"));
        return Map.of("message","Teacher assignment created");
    }

    @DeleteMapping("/scopes")
    public Map<String,String> removeScope(@RequestParam String teacherCode, @RequestParam int grade,
            @RequestParam String subjectId) {
        jdbc.update("DELETE FROM TEACHER_SCOPE WHERE teacher_id=? AND grade_level=? AND subject_id=?",
            teacherCode,grade,subjectId);
        return Map.of("message","Assignment removed");
    }

    @GetMapping("/scopes")
    public List<Map<String,Object>> allScopes() {
        return jdbc.queryForList("SELECT * FROM TEACHER_SCOPE ORDER BY teacher_id,grade_level,subject_id");
    }

    // Uses your EXISTING STUDENT table: no new Student Java class is created.
    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> addStudent(@RequestBody SmsDtos.NewStudent value) {
        String id = required(value.studentId(),"Student ID");
        String name = required(value.studentName(),"Student name");
        java.time.LocalDate birth;
        try { birth=java.time.LocalDate.parse(required(value.birthday(),"Birthday")); }
        catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("Use birthday YYYY-MM-DD"); }
        jdbc.update("INSERT INTO STUDENT(Studentid,studentname,birthday,studentaddress) VALUES(?,?,?,?)",
            id,name,birth,required(value.studentAddress(),"Address"));
        return Map.of("message","Student registered","studentId",id);
    }

    @GetMapping("/students/{studentId}/profile")
    public Map<String,Object> studentProfile(@PathVariable String studentId) {
        return teachers.profile(studentId);
    }

    @GetMapping("/marks")
    public List<Map<String,Object>> studentSubjectMarks(@RequestParam String studentId,
            @RequestParam String subjectId) {
        return jdbc.queryForList("SELECT student_id,subject_id,term_name,mark,grade_number,entered_by,updated_at " +
            "FROM sms_subject_marks WHERE student_id=? AND subject_id=? ORDER BY term_name",studentId,subjectId);
    }

    @PostMapping("/enrollments")
    @Transactional
    public Map<String,Object> enroll(@RequestBody SmsDtos.Enrollment value) {
        int grade = value.gradeNumber();
        if (grade<1 || grade>10) throw new IllegalArgumentException("Grade must be 1–10 for this enrollment endpoint");
        String student = required(value.studentId(),"Student ID");
        String seat = value.seatType()==null?"ORDINARY":value.seatType().toUpperCase(Locale.ROOT);
        if (!seat.equals("ORDINARY") && !seat.equals("SCHOLARSHIP"))
            throw new IllegalArgumentException("Seat must be ORDINARY or SCHOLARSHIP");
        if (seat.equals("SCHOLARSHIP") && grade!=6)
            throw new IllegalArgumentException("Scholarship seats apply only to Grade 6");
        jdbc.queryForObject("SELECT setup_id FROM SYSTEM_SETUP WHERE setup_id=1 FOR UPDATE", Integer.class);
        Integer studentExists = jdbc.queryForObject("SELECT COUNT(*) FROM STUDENT WHERE Studentid=?",
            Integer.class,student);
        if(studentExists==null || studentExists==0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Register the student before enrollment");
        List<Map<String,Object>> current = jdbc.queryForList(
            "SELECT grade_number,seat_type,active FROM sms_student_enrollment WHERE student_id=?",student);
        if(value.active()) {
            boolean unchanged = !current.isEmpty() && ((Number)current.get(0).get("grade_number")).intValue()==grade &&
                seat.equals(current.get(0).get("seat_type")) && (Boolean.TRUE.equals(current.get(0).get("active")) ||
                    (current.get(0).get("active") instanceof Number n && n.intValue() == 1));
            if(!unchanged) {
                int capacity = seat.equals("SCHOLARSHIP") ? setting("GRADE6_SCHOLARSHIP_EXTRA_SEATS") : setting("GRADE1_CAPACITY");
                Integer used=jdbc.queryForObject(
                    "SELECT COUNT(*) FROM sms_student_enrollment WHERE grade_number=? AND seat_type=? AND active=TRUE",
                    Integer.class,grade,seat);
                if (used!=null && used>=capacity)
                    throw new ResponseStatusException(HttpStatus.CONFLICT,"No available seat in this grade/seat category");
            }
        }
        jdbc.update("INSERT INTO sms_student_enrollment(student_id,grade_number,seat_type,active) VALUES(?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE grade_number=VALUES(grade_number),seat_type=VALUES(seat_type),active=VALUES(active)",
            student,grade,seat,value.active());
        return Map.of("studentId",student,"grade",grade,"active",value.active(),"seatType",seat);
    }

    @GetMapping("/enrollments")
    public List<Map<String,Object>> enrollments(@RequestParam(required=false) Integer grade) {
        if(grade==null) return jdbc.queryForList("SELECT * FROM sms_student_enrollment ORDER BY grade_number,student_id");
        return jdbc.queryForList("SELECT * FROM sms_student_enrollment WHERE grade_number=? ORDER BY student_id",grade);
    }

    @PostMapping("/properties")
    public Map<String,String> property(@RequestBody SmsDtos.Property value) {
        if(value.quantity()<0) throw new IllegalArgumentException("Quantity cannot be negative");
        String id=required(value.propertyId(),"Property ID");
        jdbc.update("INSERT INTO sms_property(property_id,property_name,quantity,condition_note) VALUES(?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE property_name=VALUES(property_name),quantity=VALUES(quantity)," +
            "condition_note=VALUES(condition_note)",
            id,required(value.propertyName(),"Property name"),value.quantity(),value.conditionNote());
        return Map.of("message","Property record saved","propertyId",id);
    }

    @GetMapping("/properties")
    public List<Map<String,Object>> properties() { return jdbc.queryForList("SELECT * FROM sms_property ORDER BY property_name"); }

    @GetMapping("/properties/count")
    public Map<String,Object> propertyCount() {
        Long count=jdbc.queryForObject("SELECT COALESCE(SUM(quantity),0) FROM sms_property",Long.class);
        return Map.of("totalUnits",count==null?0:count);
    }

    @DeleteMapping("/properties/{propertyId}")
    public Map<String,String> deleteProperty(@PathVariable String propertyId) {
        jdbc.update("DELETE FROM sms_property WHERE property_id=?",propertyId);
        return Map.of("message","Property removed");
    }

    @GetMapping("/dashboard")
    public Map<String,Object> dashboard() {
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("registeredStudents",scalar("SELECT COUNT(*) FROM STUDENT"));
        result.put("teacherAccounts",scalar("SELECT COUNT(*) FROM APP_USER WHERE role='TEACHER' AND enabled=TRUE"));
        result.put("propertyUnits",scalar("SELECT COALESCE(SUM(quantity),0) FROM sms_property"));
        result.put("grade1Applications",scalar("SELECT COUNT(*) FROM sms_grade1_application"));
        result.put("grade1Selected",scalar("SELECT COUNT(*) FROM sms_grade1_application WHERE status='SELECTED'"));
        result.put("enrolledByGrade",jdbc.queryForList("SELECT grade_number,COUNT(*) AS student_count " +
            "FROM sms_student_enrollment WHERE active=TRUE GROUP BY grade_number ORDER BY grade_number"));
        result.put("intermediateApplications",jdbc.queryForList("SELECT grade_number,status,COUNT(*) AS student_count " +
            "FROM sms_intermediate_application GROUP BY grade_number,status ORDER BY grade_number,status"));
        result.put("alByStream",jdbc.queryForList("SELECT c.stream_name,c.seat_limit," +
            "(SELECT COUNT(*) FROM sms_al_application a WHERE a.stream_name=c.stream_name) AS applications," +
            "(SELECT COUNT(*) FROM sms_al_application a WHERE a.stream_name=c.stream_name AND a.status='SELECTED') AS selected " +
            "FROM sms_al_stream_capacity c ORDER BY c.stream_name"));
        return result;
    }

    private long scalar(String sql) {
        Long x=jdbc.queryForObject(sql,Long.class); return x==null?0:x;
    }
    private int setting(String key) {
        Integer x=jdbc.queryForObject("SELECT setting_value FROM sms_admission_setting WHERE setting_key=?",Integer.class,key);
        return x==null?0:x;
    }
    static String required(String value,String name) {
        if(value==null||value.isBlank()) throw new IllegalArgumentException(name+" is required");
        return value.trim();
    }
}
