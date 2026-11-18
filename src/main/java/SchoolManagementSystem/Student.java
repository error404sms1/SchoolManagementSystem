package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "STUDENT")
public class Student {

    @Id
    @Column(
            name = "Studentid",
            nullable = false,
            length = 255
    )
    private String studentId;

    @Column(
            name = "studentname",
            length = 255
    )
    private String studentName;

    @Column(
            name = "studentaddress",
            length = 255
    )
    private String studentAddress;

    @Column(
            name = "birthday",
            length = 255
    )
    private String birthday;

    @Column(
            name = "parent_name",
            length = 255
    )
    private String parentName;

    @Column(
            name = "parent_contact_no",
            length = 50
    )
    private String parentContactNo;

    public Student() {
    }

    public Student(
            String studentId,
            String studentName,
            String studentAddress,
            String birthday
    ) {

        this.studentId = studentId;
        this.studentName = studentName;
        this.studentAddress = studentAddress;
        this.birthday = birthday;
    }

    public Student(
            String studentId,
            String studentName,
            String studentAddress,
            String birthday,
            String parentName,
            String parentContactNo
    ) {

        this.studentId = studentId;
        this.studentName = studentName;
        this.studentAddress = studentAddress;
        this.birthday = birthday;
        this.parentName = parentName;
        this.parentContactNo =
                parentContactNo;
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

    public String getStudentAddress() {
        return studentAddress;
    }

    public void setStudentAddress(
            String studentAddress
    ) {
        this.studentAddress =
                studentAddress;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(
            String birthday
    ) {
        this.birthday = birthday;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(
            String parentName
    ) {
        this.parentName = parentName;
    }

    public String getParentContactNo() {
        return parentContactNo;
    }

    public void setParentContactNo(
            String parentContactNo
    ) {
        this.parentContactNo =
                parentContactNo;
    }
}
