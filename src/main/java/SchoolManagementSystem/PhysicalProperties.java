package SchoolManagementSystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "physical_properties")
public class PhysicalProperties {

    @Id
    @Column(
            name = "property_id",
            nullable = false,
            length = 100
    )
    private String propertyId;

    @Column(
            name = "property_name",
            nullable = false,
            length = 255
    )
    private String propertyName;

    public PhysicalProperties() {
    }

    public PhysicalProperties(
            String propertyId,
            String propertyName
    ) {
        this.propertyId = propertyId;
        this.propertyName = propertyName;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(
            String propertyId
    ) {
        this.propertyId = propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(
            String propertyName
    ) {
        this.propertyName = propertyName;
    }
}
