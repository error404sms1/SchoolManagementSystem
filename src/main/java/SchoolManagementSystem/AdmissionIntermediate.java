package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admission_intermediate")
public class AdmissionIntermediate
        extends Admission {

    @Column(name = "max_student_count")
    private int maxStudentCount;

    @Column(name = "school_cut_off")
    private int schoolCutOff;

    @Column(name = "students_g2")
    private int studentsG2;

    @Column(name = "students_g3")
    private int studentsG3;

    @Column(name = "students_g4")
    private int studentsG4;

    @Column(name = "students_g5")
    private int studentsG5;

    @Column(name = "students_g6")
    private int studentsG6;

    @Column(name = "students_g7")
    private int studentsG7;

    @Column(name = "students_g8")
    private int studentsG8;

    @Column(name = "students_g9")
    private int studentsG9;

    @Column(name = "students_g10")
    private int studentsG10;

    public AdmissionIntermediate() {
    }

    public AdmissionIntermediate(
            String admissionId
    ) {
        super(admissionId);
    }

    public boolean isRegistrationAvailable(
            int grade
    ) {

        int current =
                switch (grade) {
                    case 2 -> studentsG2;
                    case 3 -> studentsG3;
                    case 4 -> studentsG4;
                    case 5 -> studentsG5;
                    case 6 -> studentsG6;
                    case 7 -> studentsG7;
                    case 8 -> studentsG8;
                    case 9 -> studentsG9;
                    case 10 -> studentsG10;

                    default ->
                            throw new IllegalArgumentException(
                                    "Grade must be between 2 and 10"
                            );
                };

        return current < maxStudentCount;
    }

    public boolean checkSecondary(
            int scholarshipMark
    ) {
        return scholarshipMark >=
                schoolCutOff;
    }

    public boolean checkSecondary(
            boolean parentTransferredFar,
            boolean homeChangedFar
    ) {
        return parentTransferredFar ||
                homeChangedFar;
    }

    public int getMaxStudentCount() {
        return maxStudentCount;
    }

    public void setMaxStudentCount(
            int maxStudentCount
    ) {
        this.maxStudentCount =
                maxStudentCount;
    }

    public int getSchoolCutOff() {
        return schoolCutOff;
    }

    public void setSchoolCutOff(
            int schoolCutOff
    ) {
        this.schoolCutOff =
                schoolCutOff;
    }

    public int getStudentsG2() {
        return studentsG2;
    }

    public void setStudentsG2(int studentsG2) {
        this.studentsG2 = studentsG2;
    }

    public int getStudentsG3() {
        return studentsG3;
    }

    public void setStudentsG3(int studentsG3) {
        this.studentsG3 = studentsG3;
    }

    public int getStudentsG4() {
        return studentsG4;
    }

    public void setStudentsG4(int studentsG4) {
        this.studentsG4 = studentsG4;
    }

    public int getStudentsG5() {
        return studentsG5;
    }

    public void setStudentsG5(int studentsG5) {
        this.studentsG5 = studentsG5;
    }

    public int getStudentsG6() {
        return studentsG6;
    }

    public void setStudentsG6(int studentsG6) {
        this.studentsG6 = studentsG6;
    }

    public int getStudentsG7() {
        return studentsG7;
    }

    public void setStudentsG7(int studentsG7) {
        this.studentsG7 = studentsG7;
    }

    public int getStudentsG8() {
        return studentsG8;
    }

    public void setStudentsG8(int studentsG8) {
        this.studentsG8 = studentsG8;
    }

    public int getStudentsG9() {
        return studentsG9;
    }

    public void setStudentsG9(int studentsG9) {
        this.studentsG9 = studentsG9;
    }

    public int getStudentsG10() {
        return studentsG10;
    }

    public void setStudentsG10(
            int studentsG10
    ) {
        this.studentsG10 =
                studentsG10;
    }
}
