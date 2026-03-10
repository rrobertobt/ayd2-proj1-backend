package edu.robertob.ayd2_p1_backend.casetypes.models.entities;

import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "case_type_stages")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CaseTypeStageModel extends BaseModel {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeModel caseType;

    @Column(nullable = false, length = 150)
    private String name;

    @Column
    private String description;

    @Column(name = "stage_order", nullable = false)
    private int stageOrder;

    @Column(nullable = false)
    private boolean active = true;
}
