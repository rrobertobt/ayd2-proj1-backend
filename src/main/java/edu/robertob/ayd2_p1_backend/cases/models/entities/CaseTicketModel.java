package edu.robertob.ayd2_p1_backend.cases.models.entities;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "case_tickets")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CaseTicketModel extends BaseModel {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectModel project;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeModel caseType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_employee_id", nullable = false)
    private EmployeeModel createdByEmployee;

    @Column(nullable = false, length = 250)
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatusEnum status = CaseStatusEnum.OPEN;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;
}
