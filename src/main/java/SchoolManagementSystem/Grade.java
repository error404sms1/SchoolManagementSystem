package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_grade")
public class Grade {

    @Id
    @Column(
            name = "student_id",
            nullable = false,
            length = 255
    )
    private String studentId;

    @Column(
            name = "grade",
            nullable = false,
            length = 50
    )
    private String grade;

    public Grade() {
    }

    public Grade(
            String studentId,
            String grade
    ) {
        this.studentId = studentId;
        this.grade = grade;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(
            String studentId
    ) {
        this.studentId = studentId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(
            String grade
    ) {
        this.grade = grade;
    }
}
