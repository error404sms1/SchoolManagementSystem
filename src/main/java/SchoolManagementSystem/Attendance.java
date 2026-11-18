package SchoolManagementSystem;

import jakarta.persistence.*;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "attendance_id")
    private Long attendanceId;

    @Column(
            name = "student_id",
            nullable = false,
            length = 255
    )
    private String studentId;

    @Column(
            name = "attendance_date",
            nullable = false,
            length = 50
    )
    private String date;

    @Column(
            name = "class_name",
            nullable = false,
            length = 100
    )
    private String className;

    @Column(
            name = "attendance_time",
            nullable = false,
            length = 50
    )
    private String time;

    public Attendance() {
    }

    public Attendance(
            String studentId,
            String date,
            String className,
            String time
    ) {
        this.studentId = studentId;
        this.date = date;
        this.className = className;
        this.time = time;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(
            Long attendanceId
    ) {
        this.attendanceId =
                attendanceId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(
            String studentId
    ) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(
            String date
    ) {
        this.date = date;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(
            String className
    ) {
        this.className = className;
    }

    public String getTime() {
        return time;
    }

    public void setTime(
            String time
    ) {
        this.time = time;
    }
}
