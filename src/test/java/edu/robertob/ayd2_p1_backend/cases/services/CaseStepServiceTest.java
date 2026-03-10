package edu.robertob.ayd2_p1_backend.cases.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStepStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.ApproveStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.AssignStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateWorklogDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.RejectStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseStepModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.WorkLogModel;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseStepRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.WorkLogRepository;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseStepServiceTest {

    @Mock CaseTicketRepository caseTicketRepository;
    @Mock CaseStepRepository caseStepRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock WorkLogRepository workLogRepository;

    @InjectMocks CaseStepService service;

    @BeforeEach
    void setUpSecurity() {
        SecurityContext ctx = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn("dev");
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void clearSecurity() { SecurityContextHolder.clearContext(); }

    // ── assignStep ────────────────────────────────────────────────────────────

    @Test
    void assignStep_throwsWhenCaseNotFound() {
        when(caseTicketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.assignStep(1L, 1L, new AssignStepDTO()));
    }

    @Test
    void assignStep_throwsWhenProjectInactive() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.INACTIVE, CaseStatusEnum.OPEN);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> service.assignStep(1L, 1L, new AssignStepDTO()));
    }

    @Test
    void assignStep_throwsWhenCaseCanceled() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.CANCELED);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> service.assignStep(1L, 1L, new AssignStepDTO()));
    }

    @Test
    void assignStep_throwsWhenStepAlreadyApproved() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.OPEN);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.APPROVED);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> service.assignStep(1L, 1L, new AssignStepDTO()));
    }

    @Test
    void assignStep_throwsWhenPreviousStepNotApproved() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.OPEN);
        CaseStepModel step1 = buildStep(1, CaseStepStatusEnum.PENDING);
        CaseStepModel step2 = buildStep(2, CaseStepStatusEnum.PENDING);
        step2.setId(2L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(2L, 1L)).thenReturn(Optional.of(step2));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step1, step2));

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(10L);
        assertThrows(BadRequestException.class, () -> service.assignStep(1L, 2L, dto));
    }

    @Test
    void assignStep_success() throws NotFoundException {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.OPEN);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.PENDING);
        EmployeeModel employee = buildEmployee(10L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));
        when(employeeRepository.findByUserId(10L)).thenReturn(Optional.of(employee));
        when(caseStepRepository.save(step)).thenReturn(step);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(10L);
        var result = service.assignStep(1L, 1L, dto);

        assertEquals(CaseStepStatusEnum.ASSIGNED.name(), result.status());
        verify(caseTicketRepository).save(ticket); // case moved to IN_PROGRESS
    }

    // ── approveStep ───────────────────────────────────────────────────────────

    @Test
    void approveStep_throwsWhenNoWorklog() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.SUBMITTED);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(workLogRepository.existsByCaseStepId(1L)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.approveStep(1L, 1L, new ApproveStepDTO()));
    }

    @Test
    void approveStep_throwsWhenInvalidStatus() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(workLogRepository.existsByCaseStepId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.approveStep(1L, 1L, new ApproveStepDTO()));
    }

    @Test
    void approveStep_lastStep_completesCase() throws NotFoundException {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.SUBMITTED);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(workLogRepository.existsByCaseStepId(1L)).thenReturn(true);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);
        when(caseStepRepository.findById(1L)).thenReturn(Optional.of(step));

        service.approveStep(1L, 1L, new ApproveStepDTO());

        assertEquals(CaseStatusEnum.COMPLETED, ticket.getStatus());
        verify(caseTicketRepository).save(ticket);
    }

    // ── rejectStep ────────────────────────────────────────────────────────────

    @Test
    void rejectStep_throwsWhenNoWorklog() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.SUBMITTED);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(workLogRepository.existsByCaseStepId(1L)).thenReturn(false);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("not done");
        assertThrows(BadRequestException.class, () -> service.rejectStep(1L, 1L, dto));
    }

    @Test
    void rejectStep_success() throws NotFoundException {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.SUBMITTED);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(workLogRepository.existsByCaseStepId(1L)).thenReturn(true);
        when(caseStepRepository.save(step)).thenReturn(step);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("needs more work");
        var result = service.rejectStep(1L, 1L, dto);

        assertEquals(CaseStepStatusEnum.IN_PROGRESS.name(), result.status());
        assertEquals("needs more work", result.rejectionReason());
    }

    // ── createWorklog ─────────────────────────────────────────────────────────

    @Test
    void createWorklog_throwsWhenCaseCanceled() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.CANCELED);
        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class,
                () -> service.createWorklog(1L, 1L, new CreateWorklogDTO()));
    }

    @Test
    void createWorklog_throwsWhenStepNotAssigned() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> service.createWorklog(1L, 1L, new CreateWorklogDTO()));
    }

    @Test
    void createWorklog_throwsWhenNotAssignedEmployee() {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.ASSIGNED);
        EmployeeModel assignedEmp = buildEmployee(99L); // different employee
        EmployeeModel callerEmp = buildEmployee(1L);
        step.setAssignedEmployee(assignedEmp);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserUsername("dev")).thenReturn(Optional.of(callerEmp));

        assertThrows(AccessDeniedException.class,
                () -> service.createWorklog(1L, 1L, new CreateWorklogDTO()));
    }

    @Test
    void createWorklog_success() throws NotFoundException {
        CaseTicketModel ticket = buildTicket(ProjectStatusEnum.ACTIVE, CaseStatusEnum.IN_PROGRESS);
        CaseStepModel step = buildStep(1, CaseStepStatusEnum.ASSIGNED);
        EmployeeModel employee = buildEmployee(1L);
        step.setAssignedEmployee(employee);

        WorkLogModel savedLog = new WorkLogModel();
        savedLog.setId(10L);
        savedLog.setCaseStep(step);
        savedLog.setEmployee(employee);
        savedLog.setComment("done");
        savedLog.setHoursSpent(2.0);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(1L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserUsername("dev")).thenReturn(Optional.of(employee));
        when(caseStepRepository.save(step)).thenReturn(step);
        when(workLogRepository.save(any())).thenReturn(savedLog);

        CreateWorklogDTO dto = new CreateWorklogDTO();
        dto.setComment("done");
        dto.setHoursSpent(2.0);
        var result = service.createWorklog(1L, 1L, dto);

        assertNotNull(result);
        assertEquals("done", result.comment());
        verify(workLogRepository).save(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CaseTicketModel buildTicket(ProjectStatusEnum projStatus, CaseStatusEnum caseStatus) {
        ProjectModel project = new ProjectModel();
        project.setId(1L);
        project.setName("Project");
        project.setStatus(projStatus);

        CaseTicketModel ticket = new CaseTicketModel();
        ticket.setId(1L);
        ticket.setProject(project);
        ticket.setStatus(caseStatus);
        ticket.setDueDate(LocalDate.now().plusDays(7));
        return ticket;
    }

    private CaseStepModel buildStep(int order, CaseStepStatusEnum status) {
        CaseTypeStageModel stage = new CaseTypeStageModel();
        stage.setId(1L);
        stage.setName("Stage " + order);

        CaseStepModel step = new CaseStepModel();
        step.setId(1L);
        step.setStepOrder(order);
        step.setStatus(status);
        step.setCaseTypeStage(stage);
        return step;
    }

    private EmployeeModel buildEmployee(Long id) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setFirst_name("Dev");
        emp.setLast_name("User");
        return emp;
    }
}
