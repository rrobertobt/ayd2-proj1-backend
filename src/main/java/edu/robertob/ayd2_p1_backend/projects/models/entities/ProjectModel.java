package edu.robertob.ayd2_p1_backend.projects.models.entities;

import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "projects")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectModel extends BaseModel {

    @Column(nullable = false, length = 200)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatusEnum status;
}
