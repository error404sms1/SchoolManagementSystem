package SchoolManagementSystem;

import java.math.BigDecimal;
import java.time.LocalDate;

// Request data models only. These are NOT JPA entities and do NOT rename any
// of your existing Student, Teacher, SubjectMarks, or Admission* classes.
public final class SmsDtos {
    private SmsDtos() {}
    public record Register(String username, String password) {}
    public record Login(String username, String password) {}
    public record CreateAccount(String username, String password, String role, String teacherCode) {}
    public record Scope(String teacherCode, int gradeNumber, String subjectId) {}
    public record NewStudent(String studentId, String studentName, String birthday, String studentAddress) {}
    public record Enrollment(String studentId, int gradeNumber, String seatType, boolean active) {}
    public record Attendance(String studentId, LocalDate date, String subjectId, boolean present) {}
    public record Mark(String studentId, String subjectId, String termName, BigDecimal mark) {}
    public record ProfileNote(String studentId, String subjectId, String achievements, String behaviour) {}
    public record NewAssignment(String title, String description, int gradeNumber, String subjectId, String linkUrl) {}
    public record Property(String propertyId, String propertyName, int quantity, String conditionNote) {}
    public record Capacity(int limit) {}
    public record Grade6Settings(int additionalSeats, int scholarshipCutoff) {}
    public record Grade1(String admissionId, String studentName, LocalDate birthDate, LocalDate assignDate,
                         BigDecimal distance, Integer marks, Boolean parentInSchool, Boolean siblingInSchool,
                         Boolean parentTeaPrin, Boolean parentOfficer, Boolean parentAbroad) {}
    public record Intermediate(String studentId, int gradeNumber, Integer scholarshipMark,
                               BigDecimal parentTransferKm, BigDecimal homeMoveKm) {}
    public record Al(String studentId, String streamName, boolean currentSchoolStudent) {}
}
