package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_profile")
public class StudentProfile {

    @Id
    @Column(
            name = "student_id",
            nullable = false,
            length = 255
    )
    private String studentId;

    @Column(
            name = "student_name",
            length = 255
    )
    private String studentName;

    @Column(
            name = "student_birth_date",
            length = 50
    )
    private String studentBirthDate;

    @Column(
            name = "student_address",
            length = 500
    )
    private String studentAddress;

    @Column(
            name = "achievements",
            columnDefinition = "TEXT"
    )
    private String achievements;

    @Column(
            name = "behaviour",
            columnDefinition = "TEXT"
    )
    private String behaviour;

    @Column(name = "average_marks")
    private double averageMarks;

    public StudentProfile() {
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(
            String studentId
    ) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(
            String studentName
    ) {
        this.studentName = studentName;
    }

    public String getStudentBirthDate() {
        return studentBirthDate;
    }

    public void setStudentBirthDate(
            String studentBirthDate
    ) {
        this.studentBirthDate =
                studentBirthDate;
    }

    public String getStudentAddress() {
        return studentAddress;
    }

    public void setStudentAddress(
            String studentAddress
    ) {
        this.studentAddress =
                studentAddress;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(
            String achievements
    ) {
        this.achievements = achievements;
    }

    public String getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(
            String behaviour
    ) {
        this.behaviour = behaviour;
    }

    public double getAverageMarks() {
        return averageMarks;
    }

    public void setAverageMarks(
            double averageMarks
    ) {
        this.averageMarks =
                averageMarks;
    }
}
