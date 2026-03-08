package edu.robertob.ayd2_p1_backend.casetypes.models.entities;

import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "case_types")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CaseTypeModel extends BaseModel {

    @Column(nullable = false, length = 150)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
