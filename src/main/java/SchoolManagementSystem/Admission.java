package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Admission {

    @Id
    @Column(name = "admission_id", nullable = false, length = 100)
    protected String admissionId;

    @Column(name = "marks", nullable = false)
    protected int marks = 0;

    protected Admission() {
    }

    protected Admission(String admissionId) {
        this.admissionId = admissionId;
    }

    public String getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(String admissionId) {
        this.admissionId = admissionId;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }
}
