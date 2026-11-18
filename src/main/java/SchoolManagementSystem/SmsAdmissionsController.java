package SchoolManagementSystem;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@RestController
public class SmsAdmissionsController {
    private final JdbcTemplate jdbc;
    public SmsAdmissionsController(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    private static boolean yes(Boolean b) { return Boolean.TRUE.equals(b); }
    private static boolean truthy(Object value) {
        if(value instanceof Boolean b) return b;
        if(value instanceof Number n) return n.intValue()!=0;
        return false;
    }
    private static LocalDate asDate(Object value) {
        if(value instanceof LocalDate d) return d;
        if(value instanceof java.sql.Date d) return d.toLocalDate();
        if(value==null) throw new IllegalArgumentException("Missing date in Grade 1 application");
        return LocalDate.parse(value.toString());
    }
    private static BigDecimal nonnegative(BigDecimal value, String name) {
        BigDecimal n=value==null?BigDecimal.ZERO:value;
        if (n.signum()<0) throw new IllegalArgumentException(name+" must not be negative");
        return n;
    }
    private int setting(String key) {
        Integer x=jdbc.queryForObject("SELECT setting_value FROM sms_admission_setting WHERE setting_key=?",
            Integer.class,key);
        return x==null?0:x;
    }
    private void setSetting(String key,int value) {
        if (value<0) throw new IllegalArgumentException("Setting cannot be negative");
        jdbc.update("INSERT INTO sms_admission_setting(setting_key,setting_value) VALUES(?,?) " +
            "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)",key,value);
    }
    private void lockSelection() {
        jdbc.queryForObject("SELECT setup_id FROM SYSTEM_SETUP WHERE setup_id=1 FOR UPDATE",Integer.class);
    }

    // New API path deliberately avoids duplicate request mappings with the
    // EXISTING AdmissionGrade1Controller. Do not rename/delete that controller.
    @GetMapping("/api/admin/sms-admissions/grade1")
    public List<Map<String,Object>> listGrade1() {
        return jdbc.queryForList("SELECT admission_id AS admissionId,student_name AS studentName," +
            "marks,assign_date AS assignDate,birth_date AS birthDate,distance," +
            "parent_in_school AS parentInSchool,sibling_in_school AS siblingInSchool," +
            "parent_tea_prin AS parentTeaPrin,parent_officer AS parentOfficer," +
            "parent_abroad AS parentAbroad,status,submitted_at AS submittedAt " +
            "FROM sms_grade1_application ORDER BY submitted_at,id");
    }

    @GetMapping("/api/admin/sms-admissions/grade1/{admissionId}")
    public Map<String,Object> grade1(@PathVariable String admissionId) {
        return jdbc.queryForList("SELECT admission_id AS admissionId,student_name AS studentName," +
            "marks,assign_date AS assignDate,birth_date AS birthDate,distance," +
            "parent_in_school AS parentInSchool,sibling_in_school AS siblingInSchool," +
            "parent_tea_prin AS parentTeaPrin,parent_officer AS parentOfficer," +
            "parent_abroad AS parentAbroad,status,submitted_at AS submittedAt " +
            "FROM sms_grade1_application WHERE admission_id=?",admissionId)
            .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Admission not found"));
    }

    @PostMapping("/api/admin/sms-admissions/grade1")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> addGrade1(@RequestBody SmsDtos.Grade1 value) {
        validateGrade1(value);
        jdbc.update("INSERT INTO sms_grade1_application(admission_id,student_name,marks,assign_date,birth_date," +
            "distance,parent_in_school,sibling_in_school,parent_tea_prin,parent_officer,parent_abroad) " +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?)",
            SmsAdminController.required(value.admissionId(),"Admission ID"),value.studentName(),
            value.marks(),value.assignDate()==null?LocalDate.now():value.assignDate(),value.birthDate(),
            value.distance(),yes(value.parentInSchool()),yes(value.siblingInSchool()),
            yes(value.parentTeaPrin()),yes(value.parentOfficer()),yes(value.parentAbroad()));
        return grade1(value.admissionId());
    }

    @PutMapping("/api/admin/sms-admissions/grade1/{admissionId}")
    public Map<String,Object> editGrade1(@PathVariable String admissionId,@RequestBody SmsDtos.Grade1 value) {
        validateGrade1(value);
        int n=jdbc.update("UPDATE sms_grade1_application SET student_name=?,marks=?,assign_date=?,birth_date=?," +
            "distance=?,parent_in_school=?,sibling_in_school=?,parent_tea_prin=?,parent_officer=?,parent_abroad=? " +
            "WHERE admission_id=?",value.studentName(),value.marks(),
            value.assignDate()==null?LocalDate.now():value.assignDate(),value.birthDate(),value.distance(),
            yes(value.parentInSchool()),yes(value.siblingInSchool()),yes(value.parentTeaPrin()),
            yes(value.parentOfficer()),yes(value.parentAbroad()),admissionId);
        if(n==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Admission not found");
        return grade1(admissionId);
    }

    @DeleteMapping("/api/admin/sms-admissions/grade1/{admissionId}")
    public Map<String,String> deleteGrade1(@PathVariable String admissionId) {
        jdbc.update("DELETE FROM sms_grade1_application WHERE admission_id=?",admissionId);
        return Map.of("message","Application deleted");
    }
    private void validateGrade1(SmsDtos.Grade1 value) {
        if(value.birthDate()==null || value.birthDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("A valid birth date is required");
        if(value.distance()!=null) nonnegative(value.distance(),"Distance");
        if(value.marks()!=null && value.marks()<0)
            throw new IllegalArgumentException("Marks cannot be negative");
    }

    @GetMapping("/api/admin/sms-admissions/grade1/{admissionId}/result")
    public Map<String,Object> grade1Result(@PathVariable String admissionId) {
        Map<String,Object> a=grade1(admissionId);
        LocalDate birthday=asDate(a.get("birthDate"));
        LocalDate date=asDate(a.get("assignDate"));
        boolean ageEligible=Period.between(birthday,date).getYears()>=5;
        int score=0;
        Object distance=a.get("distance");
        if(distance instanceof Number n && n.doubleValue()<=7) score+=50;
        if(truthy(a.get("parentInSchool"))) score+=25;
        if(truthy(a.get("siblingInSchool"))) score+=15;
        if(truthy(a.get("parentTeaPrin"))) score+=5;
        if(truthy(a.get("parentOfficer"))) score+=4;
        if(truthy(a.get("parentAbroad"))) score+=1;
        return Map.of("admissionId",admissionId,"ageEligible",ageEligible,"calculatedMarks",score,
            "status",a.get("status"));
    }

    @GetMapping("/api/admin/admissions/grade1/capacity")
    public Map<String,Integer> grade1Capacity() {
        return Map.of("limit",setting("GRADE1_CAPACITY"));
    }
    @PutMapping("/api/admin/admissions/grade1/capacity")
    public Map<String,Integer> updateGrade1Capacity(@RequestBody SmsDtos.Capacity value) {
        setSetting("GRADE1_CAPACITY",value.limit());
        return grade1Capacity();
    }

    // Recalculate selection deterministically. All applications stay saved.
    @PostMapping("/api/admin/admissions/grade1/select")
    @Transactional
    public Map<String,Object> selectGrade1() {
        lockSelection();
        int cap=setting("GRADE1_CAPACITY");
        jdbc.update("UPDATE sms_grade1_application SET status='WAITLISTED'");
        List<Long> ids=jdbc.query("SELECT id FROM sms_grade1_application ORDER BY submitted_at,id LIMIT ?",
            (rs,i)->rs.getLong(1),cap);
        for(Long id:ids) jdbc.update("UPDATE sms_grade1_application SET status='SELECTED' WHERE id=?",id);
        Long total=jdbc.queryForObject("SELECT COUNT(*) FROM sms_grade1_application",Long.class);
        return Map.of("capacity",cap,"applications",total==null?0:total,
            "selected",ids.size(),"waiting",(total==null?0:total)-ids.size(),
            "selectedAdmissions",jdbc.queryForList("SELECT admission_id,submitted_at FROM sms_grade1_application " +
                "WHERE status='SELECTED' ORDER BY submitted_at,id"));
    }

    @GetMapping("/api/admin/admissions/grade1/selected")
    public List<Map<String,Object>> grade1Selected() {
        return jdbc.queryForList("SELECT admission_id,student_name,birth_date,submitted_at " +
            "FROM sms_grade1_application WHERE status='SELECTED' ORDER BY submitted_at,id");
    }

    // Intermediate admissions: 2-10, 50 km relocation priority; Grade 6
    // scholarship priority + separate special scholarship seats.
    @GetMapping("/api/admin/admissions/intermediate")
    public List<Map<String,Object>> intermediate(@RequestParam(required=false) Integer grade) {
        if (grade==null) return jdbc.queryForList("SELECT * FROM sms_intermediate_application ORDER BY grade_number,submitted_at,id");
        validateIntermediateGrade(grade);
        return jdbc.queryForList("SELECT * FROM sms_intermediate_application WHERE grade_number=? ORDER BY submitted_at,id",grade);
    }

    @PostMapping("/api/admin/admissions/intermediate")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> applyIntermediate(@RequestBody SmsDtos.Intermediate value) {
        validateIntermediateGrade(value.gradeNumber());
        if(value.scholarshipMark()!=null && value.scholarshipMark()<0) throw new IllegalArgumentException("Invalid scholarship marks");
        jdbc.update("INSERT INTO sms_intermediate_application(student_id,grade_number,scholarship_mark," +
            "parent_transfer_km,home_move_km) VALUES(?,?,?,?,?)",
            SmsAdminController.required(value.studentId(),"Student ID"),value.gradeNumber(),value.scholarshipMark(),
            nonnegative(value.parentTransferKm(),"Parent workplace transfer distance"),
            nonnegative(value.homeMoveKm(),"Home moving distance"));
        return Map.of("message","Intermediate application recorded", "grade", value.gradeNumber());
    }

    @GetMapping("/api/admin/admissions/intermediate/grade6-settings")
    public Map<String,Integer> grade6Settings() {
        return Map.of("additionalSeats",setting("GRADE6_SCHOLARSHIP_EXTRA_SEATS"),
            "scholarshipCutoff",setting("GRADE6_SCHOLARSHIP_CUTOFF"));
    }
    @PutMapping("/api/admin/admissions/intermediate/grade6-settings")
    public Map<String,Integer> updateGrade6Settings(@RequestBody SmsDtos.Grade6Settings value) {
        setSetting("GRADE6_SCHOLARSHIP_EXTRA_SEATS",value.additionalSeats());
        setSetting("GRADE6_SCHOLARSHIP_CUTOFF",value.scholarshipCutoff());
        return grade6Settings();
    }

    @PostMapping("/api/admin/admissions/intermediate/select/{grade}")
    @Transactional
    public Map<String,Object> selectIntermediate(@PathVariable int grade) {
        validateIntermediateGrade(grade);
        lockSelection();
        int base=setting("GRADE1_CAPACITY");
        int cutoff=setting("GRADE6_SCHOLARSHIP_CUTOFF");
        int extra=grade==6?setting("GRADE6_SCHOLARSHIP_EXTRA_SEATS"):0;
        Integer occupiedOrd=jdbc.queryForObject("SELECT COUNT(*) FROM sms_student_enrollment " +
            "WHERE grade_number=? AND seat_type='ORDINARY' AND active=TRUE",Integer.class,grade);
        Integer occupiedSch=jdbc.queryForObject("SELECT COUNT(*) FROM sms_student_enrollment " +
            "WHERE grade_number=? AND seat_type='SCHOLARSHIP' AND active=TRUE",Integer.class,grade);
        int ordinaryOpen=Math.max(0,base-(occupiedOrd==null?0:occupiedOrd));
        int specialOpen=Math.max(0,extra-(occupiedSch==null?0:occupiedSch));
        jdbc.update("UPDATE sms_intermediate_application SET status='WAITLISTED' WHERE grade_number=?",grade);
        List<Map<String,Object>> ranked=jdbc.queryForList(
            "SELECT id,student_id,scholarship_mark,parent_transfer_km,home_move_km," +
            "CASE WHEN ?=6 AND scholarship_mark>=? THEN 1 ELSE 0 END AS scholarship_ok," +
            "CASE WHEN parent_transfer_km>=50 OR home_move_km>=50 THEN 1 ELSE 0 END AS relocation_ok " +
            "FROM sms_intermediate_application WHERE grade_number=? " +
            "ORDER BY CASE WHEN ?=6 AND scholarship_mark>=? THEN 0 " +
            "WHEN parent_transfer_km>=50 OR home_move_km>=50 THEN 1 ELSE 2 END,submitted_at,id",
            grade,cutoff,grade,grade,cutoff);
        List<Long> selectedSpecial=new ArrayList<>();
        Set<Long> already=new HashSet<>();
        if(grade==6) for(Map<String,Object> app:ranked) {
            if(selectedSpecial.size()>=specialOpen) break;
            if(((Number)app.get("scholarship_ok")).intValue()==1) {
                long id=((Number)app.get("id")).longValue();
                selectedSpecial.add(id); already.add(id);
                jdbc.update("UPDATE sms_intermediate_application SET status='SELECTED_SCHOLARSHIP' WHERE id=?",id);
            }
        }
        List<Long> selectedOrdinary=new ArrayList<>();
        for(Map<String,Object> app:ranked) {
            if(selectedOrdinary.size()>=ordinaryOpen) break;
            long id=((Number)app.get("id")).longValue();
            if(already.contains(id)) continue;
            selectedOrdinary.add(id);
            jdbc.update("UPDATE sms_intermediate_application SET status='SELECTED_ORDINARY' WHERE id=?",id);
        }
        return Map.of("grade",grade,"ordinaryCapacity",base,"ordinaryVacancies",ordinaryOpen,
            "specialVacancies",specialOpen,"selectedOrdinary",selectedOrdinary.size(),
            "selectedScholarship",selectedSpecial.size(),
            "selectedStudents",jdbc.queryForList("SELECT student_id,status,submitted_at FROM sms_intermediate_application " +
                "WHERE grade_number=? AND status<>'WAITLISTED' ORDER BY status,submitted_at,id",grade));
    }
    private void validateIntermediateGrade(int grade) {
        if(grade<2||grade>10) throw new IllegalArgumentException("Intermediate grade must be 2–10");
    }

    private static String stream(String s) {
        if(s==null) throw new IllegalArgumentException("Stream required");
        String name=s.trim().toUpperCase(Locale.ROOT);
        if(!Set.of("SCIENCE","TECHNOLOGY","COMMERCE","ARTS").contains(name))
            throw new IllegalArgumentException("Stream must be SCIENCE, TECHNOLOGY, COMMERCE or ARTS");
        return name;
    }
    @GetMapping("/api/admin/admissions/al")
    public List<Map<String,Object>> allAl() {
        return jdbc.queryForList("SELECT * FROM sms_al_application ORDER BY stream_name,submitted_at,id");
    }
    @PostMapping("/api/admin/admissions/al")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> applyAl(@RequestBody SmsDtos.Al value) {
        String s=stream(value.streamName());
        String studentId=SmsAdminController.required(value.studentId(),"Student ID");
        Integer fromSchool=jdbc.queryForObject(
            "SELECT COUNT(*) FROM sms_student_enrollment WHERE student_id=? AND active=TRUE",
            Integer.class, studentId);
        jdbc.update("INSERT INTO sms_al_application(student_id,stream_name,current_school_student) VALUES(?,?,?)",
            studentId,s,fromSchool!=null && fromSchool>0);
        return Map.of("message","A/L application registered","stream",s);
    }
    @PutMapping("/api/admin/admissions/al/capacity/{streamName}")
    public Map<String,Object> setAlCapacity(@PathVariable String streamName,@RequestBody SmsDtos.Capacity value) {
        String s=stream(streamName);
        if(value.limit()<0) throw new IllegalArgumentException("Capacity cannot be negative");
        jdbc.update("UPDATE sms_al_stream_capacity SET seat_limit=? WHERE stream_name=?",value.limit(),s);
        return Map.of("stream",s,"limit",value.limit());
    }
    @GetMapping("/api/admin/admissions/al/capacity")
    public List<Map<String,Object>> alCapacity() {
        return jdbc.queryForList("SELECT * FROM sms_al_stream_capacity ORDER BY stream_name");
    }
    @PostMapping("/api/admin/admissions/al/select/{streamName}")
    @Transactional
    public Map<String,Object> selectAl(@PathVariable String streamName) {
        String s=stream(streamName);
        lockSelection();
        Integer cap=jdbc.queryForObject("SELECT seat_limit FROM sms_al_stream_capacity WHERE stream_name=?",Integer.class,s);
        int limit=cap==null?0:cap;
        jdbc.update("UPDATE sms_al_application SET status='WAITLISTED' WHERE stream_name=?",s);
        List<Long> selected=jdbc.query("SELECT id FROM sms_al_application WHERE stream_name=? " +
            "ORDER BY current_school_student DESC,submitted_at,id LIMIT ?",
            (rs,i)->rs.getLong(1),s,limit);
        for(Long id:selected) jdbc.update("UPDATE sms_al_application SET status='SELECTED' WHERE id=?",id);
        return Map.of("stream",s,"capacity",limit,"selected",selected.size(),
            "selectedStudents",jdbc.queryForList("SELECT student_id,current_school_student,submitted_at " +
                "FROM sms_al_application WHERE stream_name=? AND status='SELECTED' " +
                "ORDER BY current_school_student DESC,submitted_at,id",s));
    }
}
