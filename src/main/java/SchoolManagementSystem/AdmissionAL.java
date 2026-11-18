package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admission_al")
public class AdmissionAL extends Admission {

    @Column(name = "stream", nullable = false)
    private int stream;

    @Column(name = "criteria_met", nullable = false)
    private boolean criteriaMet;

    public AdmissionAL() {
    }

    public AdmissionAL(String admissionId, int stream, boolean criteriaMet) {
        super(admissionId);
        this.stream = stream;
        this.criteriaMet = criteriaMet;
    }

    public boolean isEligible() {
        if (stream < 1 || stream > 4) {
            return false;
        }

        return criteriaMet;
    }

    public String getStreamName() {
        return switch (stream) {
            case 1 -> "Science";
            case 2 -> "Technology";
            case 3 -> "Commerce";
            case 4 -> "Arts";
            default -> "Invalid";
        };
    }

    public int getStream() {
        return stream;
    }

    public void setStream(int stream) {
        this.stream = stream;
    }

    public boolean isCriteriaMet() {
        return criteriaMet;
    }

    public void setCriteriaMet(boolean criteriaMet) {
        this.criteriaMet = criteriaMet;
    }
}
