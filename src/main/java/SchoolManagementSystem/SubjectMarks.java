package SchoolManagementSystem;

import jakarta.persistence.*;

@Entity
@Table(name = "subject_marks")
public class SubjectMarks extends Marks {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id")
    private Long id;

    @Column(
            name = "subject_id",
            nullable = false,
            length = 100
    )
    private String subId;

    @Column(
            name = "subject_name",
            nullable = false,
            length = 255
    )
    private String subName;

    public SubjectMarks() {
    }

    public SubjectMarks(
            String studentId,
            String studentName,
            String subId,
            String subName,
            int marks
    ) {

        super(
                studentId,
                studentName,
                marks
        );

        this.subId = subId;
        this.subName = subName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(
            String subId
    ) {
        this.subId = subId;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(
            String subName
    ) {
        this.subName = subName;
    }
}
