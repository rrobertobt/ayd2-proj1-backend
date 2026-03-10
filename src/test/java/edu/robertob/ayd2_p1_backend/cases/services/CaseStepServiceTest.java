package edu.robertob.ayd2_p1_backend.cases.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStepStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.ApproveStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.AssignStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.RejectStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseStepModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseStepRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketRepository;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseStepServiceTest {

    @Mock private CaseTicketRepository caseTicketRepository;
    @Mock private CaseStepRepository caseStepRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private CaseStepService caseStepService;

    // ── getSteps ──────────────────────────────────────────────────────────────

    @Test
    void getSteps_caseExists_returnsStepList() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage 1", 1);
        CaseStepModel step1 = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);
        CaseStepModel step2 = buildStep(201L, ticket, stage, 2, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step1, step2));

        List<CaseStepDTO> result = caseStepService.getSteps(1L);

        assertEquals(2, result.size());
        assertEquals(200L, result.get(0).id());
        assertEquals(201L, result.get(1).id());
    }

    @Test
    void getSteps_caseNotFound_throwsNotFoundException() {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseStepService.getSteps(99L));
        verify(caseStepRepository, never()).findByCaseTicketIdOrderByStepOrderAsc(any());
    }

    @Test
    void getSteps_noSteps_returnsEmptyList() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        List<CaseStepDTO> result = caseStepService.getSteps(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getSteps_stepWithAssignedEmployee_returnsAssignedInfo() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel assignee = buildEmployee(11L, "Ana", "García");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.ASSIGNED);
        step.setAssignedEmployee(assignee);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));

        List<CaseStepDTO> result = caseStepService.getSteps(1L);

        assertEquals(11L, result.get(0).assignedEmployeeId());
        assertEquals("Ana García", result.get(0).assignedEmployeeName());
    }

    // ── assignStep ────────────────────────────────────────────────────────────

    @Test
    void assignStep_validMember_assignsAndReturnsDTO() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel assignee = buildEmployee(11L, "Ana", "García");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserId(45L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.existsByProjectIdAndEmployeeIdAndActiveTrue(1L, 11L))
                .thenReturn(true);
        when(caseStepRepository.save(step)).thenReturn(step);
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);

        CaseStepDTO result = caseStepService.assignStep(1L, 200L, dto);

        assertNotNull(result);
        assertEquals("ASSIGNED", result.status());
        verify(caseStepRepository).save(step);
        assertEquals(CaseStepStatusEnum.ASSIGNED, step.getStatus());
        assertNotNull(step.getAssignedAt());
    }

    @Test
    void assignStep_openCase_transitionToInProgress() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel assignee = buildEmployee(11L, "Ana", "García");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserId(45L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.existsByProjectIdAndEmployeeIdAndActiveTrue(1L, 11L))
                .thenReturn(true);
        when(caseStepRepository.save(step)).thenReturn(step);
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);

        caseStepService.assignStep(1L, 200L, dto);

        assertEquals(CaseStatusEnum.IN_PROGRESS, ticket.getStatus());
        verify(caseTicketRepository).save(ticket);
    }

    @Test
    void assignStep_alreadyInProgress_doesNotSaveTicketAgain() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel assignee = buildEmployee(11L, "Ana", "García");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserId(45L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.existsByProjectIdAndEmployeeIdAndActiveTrue(1L, 11L))
                .thenReturn(true);
        when(caseStepRepository.save(step)).thenReturn(step);

        caseStepService.assignStep(1L, 200L, dto);

        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void assignStep_employeeNotMember_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel outsider = buildEmployee(20L, "Outsider", "User");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserId(45L)).thenReturn(Optional.of(outsider));
        when(projectMemberRepository.existsByProjectIdAndEmployeeIdAndActiveTrue(1L, 20L))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> caseStepService.assignStep(1L, 200L, dto));
        verify(caseStepRepository, never()).save(any());
    }

    @Test
    void assignStep_approvedStep_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.APPROVED);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> caseStepService.assignStep(1L, 200L, dto));
    }

    @Test
    void assignStep_canceledCase_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.CANCELED, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> caseStepService.assignStep(1L, 200L, dto));
    }

    @Test
    void assignStep_caseNotFound_throwsNotFoundException() {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        assertThrows(NotFoundException.class,
                () -> caseStepService.assignStep(99L, 200L, dto));
    }

    @Test
    void assignStep_stepNotFound_throwsNotFoundException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(45L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> caseStepService.assignStep(1L, 999L, dto));
    }

    @Test
    void assignStep_userNotFound_throwsNotFoundException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        AssignStepDTO dto = new AssignStepDTO();
        dto.setUserId(999L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(employeeRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> caseStepService.assignStep(1L, 200L, dto));
    }

    // ── approveStep ───────────────────────────────────────────────────────────

    @Test
    void approveStep_submittedStep_withNextStep_assignsNext() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel nextEmployee = buildEmployee(11L, "Ana", "García");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage1 = buildStage(100L, caseType, "Stage 1", 1);
        CaseTypeStageModel stage2 = buildStage(101L, caseType, "Stage 2", 2);
        CaseStepModel step1 = buildStep(200L, ticket, stage1, 1, CaseStepStatusEnum.SUBMITTED);
        CaseStepModel step2 = buildStep(201L, ticket, stage2, 2, CaseStepStatusEnum.PENDING);

        ApproveStepDTO dto = new ApproveStepDTO();
        dto.setNextAssigneeUserId(50L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step1));
        when(caseStepRepository.save(step1)).thenReturn(step1);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step1, step2));
        when(employeeRepository.findByUserId(50L)).thenReturn(Optional.of(nextEmployee));
        when(projectMemberRepository.existsByProjectIdAndEmployeeIdAndActiveTrue(1L, 11L))
                .thenReturn(true);
        when(caseStepRepository.save(step2)).thenReturn(step2);
        when(caseStepRepository.findById(200L)).thenReturn(Optional.of(step1));

        CaseStepDTO result = caseStepService.approveStep(1L, 200L, dto);

        assertNotNull(result);
        assertEquals(CaseStepStatusEnum.APPROVED, step1.getStatus());
        assertNotNull(step1.getApprovedAt());
        assertEquals(CaseStepStatusEnum.ASSIGNED, step2.getStatus());
        assertNotNull(step2.getAssignedAt());
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void approveStep_lastStep_completesCase() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Only Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.SUBMITTED);

        ApproveStepDTO dto = new ApproveStepDTO();

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findById(200L)).thenReturn(Optional.of(step));

        caseStepService.approveStep(1L, 200L, dto);

        assertEquals(CaseStatusEnum.COMPLETED, ticket.getStatus());
        verify(caseTicketRepository).save(ticket);
    }

    @Test
    void approveStep_lastStep_nextAssigneeIgnored() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Only Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.SUBMITTED);

        ApproveStepDTO dto = new ApproveStepDTO();
        dto.setNextAssigneeUserId(50L); // should be ignored

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findById(200L)).thenReturn(Optional.of(step));

        caseStepService.approveStep(1L, 200L, dto);

        verify(employeeRepository, never()).findByUserId(any());
    }

    @Test
    void approveStep_inProgressStep_approvesSuccessfully() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.IN_PROGRESS);

        ApproveStepDTO dto = new ApproveStepDTO();

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findById(200L)).thenReturn(Optional.of(step));

        CaseStepDTO result = caseStepService.approveStep(1L, 200L, dto);

        assertNotNull(result);
        assertEquals(CaseStepStatusEnum.APPROVED, step.getStatus());
    }

    @Test
    void approveStep_pendingStep_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> caseStepService.approveStep(1L, 200L, new ApproveStepDTO()));
    }

    @Test
    void approveStep_nextAssigneeNotMember_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel outsider = buildEmployee(20L, "Outsider", "User");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage1 = buildStage(100L, caseType, "S1", 1);
        CaseTypeStageModel stage2 = buildStage(101L, caseType, "S2", 2);
        CaseStepModel step1 = buildStep(200L, ticket, stage1, 1, CaseStepStatusEnum.SUBMITTED);
        CaseStepModel step2 = buildStep(201L, ticket, stage2, 2, CaseStepStatusEnum.PENDING);

        ApproveStepDTO dto = new ApproveStepDTO();
        dto.setNextAssigneeUserId(50L);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step1));
        when(caseStepRepository.save(step1)).thenReturn(step1);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step1, step2));
        when(employeeRepository.findByUserId(50L)).thenReturn(Optional.of(outsider));
        when(projectMemberRepository.existsByProjectIdAndEmployeeIdAndActiveTrue(1L, 20L))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> caseStepService.approveStep(1L, 200L, dto));
    }

    @Test
    void approveStep_caseNotFound_throwsNotFoundException() {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> caseStepService.approveStep(99L, 200L, new ApproveStepDTO()));
    }

    @Test
    void approveStep_stepNotFound_throwsNotFoundException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> caseStepService.approveStep(1L, 999L, new ApproveStepDTO()));
    }

    // ── rejectStep ────────────────────────────────────────────────────────────

    @Test
    void rejectStep_submittedStep_rejectsAndSetsInProgress() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        EmployeeModel assignee = buildEmployee(11L, "Ana", "García");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.SUBMITTED);
        step.setAssignedEmployee(assignee);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Falta manejar edge cases en la validación");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);

        CaseStepDTO result = caseStepService.rejectStep(1L, 200L, dto);

        assertNotNull(result);
        assertEquals(CaseStepStatusEnum.IN_PROGRESS, step.getStatus());
        assertNotNull(step.getRejectedAt());
        assertEquals("Falta manejar edge cases en la validación", step.getRejectionReason());
    }

    @Test
    void rejectStep_inProgressStep_rejectsSuccessfully() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.IN_PROGRESS);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Correcciones necesarias");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);

        CaseStepDTO result = caseStepService.rejectStep(1L, 200L, dto);

        assertNotNull(result);
        assertEquals(CaseStepStatusEnum.IN_PROGRESS, step.getStatus());
    }

    @Test
    void rejectStep_assignedStep_rejectsSuccessfully() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.ASSIGNED);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Asignación incorrecta");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));
        when(caseStepRepository.save(step)).thenReturn(step);

        CaseStepDTO result = caseStepService.rejectStep(1L, 200L, dto);

        assertEquals(CaseStepStatusEnum.IN_PROGRESS, step.getStatus());
        assertNotNull(result);
    }

    @Test
    void rejectStep_pendingStep_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> caseStepService.rejectStep(1L, 200L, dto));
        verify(caseStepRepository, never()).save(any());
    }

    @Test
    void rejectStep_approvedStep_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.APPROVED);

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(200L, 1L)).thenReturn(Optional.of(step));

        assertThrows(BadRequestException.class,
                () -> caseStepService.rejectStep(1L, 200L, dto));
    }

    @Test
    void rejectStep_caseNotFound_throwsNotFoundException() {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");

        assertThrows(NotFoundException.class,
                () -> caseStepService.rejectStep(99L, 200L, dto));
    }

    @Test
    void rejectStep_stepNotFound_throwsNotFoundException() {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));

        RejectStepDTO dto = new RejectStepDTO();
        dto.setReason("Razón");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByIdAndCaseTicketId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> caseStepService.rejectStep(1L, 999L, dto));
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static ProjectModel buildProject(Long id, String name) {
        ProjectModel p = new ProjectModel();
        p.setId(id);
        p.setName(name);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }

    private static CaseTypeModel buildCaseType(Long id, String name) {
        CaseTypeModel ct = new CaseTypeModel();
        ct.setId(id);
        ct.setName(name);
        ct.setActive(true);
        ct.setCreatedAt(Instant.now());
        ct.setUpdatedAt(Instant.now());
        return ct;
    }

    private static CaseTypeStageModel buildStage(Long id, CaseTypeModel caseType,
                                                   String name, int order) {
        CaseTypeStageModel stage = new CaseTypeStageModel();
        stage.setId(id);
        stage.setCaseType(caseType);
        stage.setName(name);
        stage.setStageOrder(order);
        stage.setActive(true);
        stage.setCreatedAt(Instant.now());
        stage.setUpdatedAt(Instant.now());
        return stage;
    }

    private static EmployeeModel buildEmployee(Long id, String firstName, String lastName) {
        UserModel user = new UserModel();
        user.setId(id * 100);
        user.setUsername("user" + id);

        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setFirst_name(firstName);
        emp.setLast_name(lastName);
        emp.setUser(user);
        emp.setCreatedAt(Instant.now());
        emp.setUpdatedAt(Instant.now());
        return emp;
    }

    private static CaseTicketModel buildTicket(Long id, ProjectModel project,
                                                CaseTypeModel caseType, EmployeeModel employee,
                                                CaseStatusEnum status, LocalDate dueDate) {
        CaseTicketModel t = new CaseTicketModel();
        t.setId(id);
        t.setProject(project);
        t.setCaseType(caseType);
        t.setCreatedByEmployee(employee);
        t.setTitle("Test Case " + id);
        t.setStatus(status);
        t.setDueDate(dueDate);
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        return t;
    }

    private static CaseStepModel buildStep(Long id, CaseTicketModel ticket,
                                            CaseTypeStageModel stage, int order,
                                            CaseStepStatusEnum status) {
        CaseStepModel step = new CaseStepModel();
        step.setId(id);
        step.setCaseTicket(ticket);
        step.setCaseTypeStage(stage);
        step.setStepOrder(order);
        step.setStatus(status);
        step.setCreatedAt(Instant.now());
        step.setUpdatedAt(Instant.now());
        return step;
    }
}
