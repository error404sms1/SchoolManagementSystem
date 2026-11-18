package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Assignment {

    @Id
    @Column(
            name = "assignment_id",
            nullable = false,
            length = 100
    )
    protected String assiId;

    @Column(
            name = "subject_name",
            nullable = false,
            length = 255
    )
    protected String subName;

    @Column(
            name = "assignment_date",
            nullable = false,
            length = 50
    )
    protected String date;

    protected Assignment() {
    }

    protected Assignment(
            String assiId,
            String subName,
            String date
    ) {
        this.assiId = assiId;
        this.subName = subName;
        this.date = date;
    }

    public String getAssiId() {
        return assiId;
    }

    public void setAssiId(String assiId) {
        this.assiId = assiId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
