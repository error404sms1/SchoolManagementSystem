package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Marks {

    @Column(
            name = "student_id",
            nullable = false,
            length = 255
    )
    protected String studentId;

    @Column(
            name = "student_name",
            nullable = false,
            length = 255
    )
    protected String studentName;

    @Column(
            name = "marks",
            nullable = false
    )
    protected int marks;

    protected Marks() {
    }

    protected Marks(
            String studentId,
            String studentName,
            int marks
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.marks = marks;
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

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }
}
