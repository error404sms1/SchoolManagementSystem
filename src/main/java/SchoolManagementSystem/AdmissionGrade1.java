package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "admission_grade1")
public class AdmissionGrade1 extends Admission {

    @Column(name = "assign_date")
    private LocalDate assignDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "distance_km")
    private double distance;

    @Column(name = "parent_in_school")
    private boolean parentInSchool;

    @Column(name = "sibling_in_school")
    private boolean siblingInSchool;

    @Column(name = "parent_teacher_or_principal")
    private boolean parentTeaPrin;

    @Column(name = "parent_officer")
    private boolean parentOfficer;

    @Column(name = "parent_abroad")
    private boolean parentAbroad;

    public AdmissionGrade1() {
    }

    public AdmissionGrade1(
            String admissionId
    ) {
        super(admissionId);
    }

    public boolean isAgeEligible() {

        if (birthDate == null) {
            return false;
        }

        LocalDate effectiveAssignDate =
                assignDate == null
                        ? LocalDate.now()
                        : assignDate;

        Period age =
                Period.between(
                        birthDate,
                        effectiveAssignDate
                );

        return age.getYears() >= 5;
    }

    public int calculateMarks() {

        int total = 0;

        if (distance <= 7) {
            total += 50;
        }

        if (parentInSchool) {
            total += 25;
        }

        if (siblingInSchool) {
            total += 15;
        }

        if (parentTeaPrin) {
            total += 5;
        }

        if (parentOfficer) {
            total += 4;
        }

        if (parentAbroad) {
            total += 1;
        }

        this.marks = total;

        return total;
    }

    public LocalDate getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(
            LocalDate assignDate
    ) {
        this.assignDate = assignDate;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(
            LocalDate birthDate
    ) {
        this.birthDate = birthDate;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(
            double distance
    ) {
        this.distance = distance;
    }

    public boolean isParentInSchool() {
        return parentInSchool;
    }

    public void setParentInSchool(
            boolean parentInSchool
    ) {
        this.parentInSchool =
                parentInSchool;
    }

    public boolean isSiblingInSchool() {
        return siblingInSchool;
    }

    public void setSiblingInSchool(
            boolean siblingInSchool
    ) {
        this.siblingInSchool =
                siblingInSchool;
    }

    public boolean isParentTeaPrin() {
        return parentTeaPrin;
    }

    public void setParentTeaPrin(
            boolean parentTeaPrin
    ) {
        this.parentTeaPrin =
                parentTeaPrin;
    }

    public boolean isParentOfficer() {
        return parentOfficer;
    }

    public void setParentOfficer(
            boolean parentOfficer
    ) {
        this.parentOfficer =
                parentOfficer;
    }

    public boolean isParentAbroad() {
        return parentAbroad;
    }

    public void setParentAbroad(
            boolean parentAbroad
    ) {
        this.parentAbroad =
                parentAbroad;
    }
}
