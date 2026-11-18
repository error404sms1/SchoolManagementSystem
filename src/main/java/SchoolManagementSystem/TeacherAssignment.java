package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher_assignment")
public class TeacherAssignment
        extends Assignment {

    @Column(
            name = "assignment_link",
            length = 1000
    )
    private String link;

    public TeacherAssignment() {
    }

    public TeacherAssignment(
            String assiId,
            String subName,
            String date
    ) {
        super(
                assiId,
                subName,
                date
        );
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAssignmentLink() {
        return link;
    }
}
