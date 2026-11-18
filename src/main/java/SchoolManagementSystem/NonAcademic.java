package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "non_academic")
public class NonAcademic {

    @Id
    @Column(
            name = "employee_id",
            nullable = false,
            length = 100
    )
    private String employeeId;

    @Column(
            name = "employee_name",
            nullable = false,
            length = 255
    )
    private String employeeName;

    public NonAcademic() {
    }

    public NonAcademic(
            String employeeId,
            String employeeName
    ) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(
            String employeeId
    ) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(
            String employeeName
    ) {
        this.employeeName = employeeName;
    }
}
