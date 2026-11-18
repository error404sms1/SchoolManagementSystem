package SchoolManagementSystem;

import jakarta.persistence.*;

@Entity
@Table(name = "assignment_marks")
public class AssignmentMarks extends Marks {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id")
    private Long id;

    @Column(
            name = "assignment_id",
            nullable = false,
            length = 100
    )
    private String assiId;

    @Column(
            name = "mark_date",
            nullable = false,
            length = 50
    )
    private String date;

    public AssignmentMarks() {
    }

    public AssignmentMarks(
            String studentId,
            String studentName,
            String assiId,
            String date,
            int marks
    ) {

        super(
                studentId,
                studentName,
                marks
        );

        this.assiId = assiId;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssiId() {
        return assiId;
    }

    public void setAssiId(
            String assiId
    ) {
        this.assiId = assiId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(
            String date
    ) {
        this.date = date;
    }
}
