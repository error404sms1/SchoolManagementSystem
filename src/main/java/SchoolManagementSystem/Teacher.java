package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @Column(
            name = "teacher_id",
            nullable = false,
            length = 100
    )
    private String teacherId;

    @Column(
            name = "teacher_name",
            nullable = false,
            length = 255
    )
    private String teacherName;

    @Column(
            name = "nic",
            length = 30
    )
    private String nic;

    @Column(
            name = "address",
            length = 500
    )
    private String address;

    public Teacher() {
    }

    public Teacher(
            String teacherId,
            String teacherName,
            String nic,
            String address
    ) {

        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.nic = nic;
        this.address = address;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(
            String teacherId
    ) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(
            String teacherName
    ) {
        this.teacherName = teacherName;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(
            String nic
    ) {
        this.nic = nic;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(
            String address
    ) {
        this.address = address;
    }
}
