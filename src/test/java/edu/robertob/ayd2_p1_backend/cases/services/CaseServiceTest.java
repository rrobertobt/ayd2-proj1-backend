package edu.robertob.ayd2_p1_backend.cases.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStepStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CancelCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CaseFilterDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.UpdateDueDateDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseSummaryDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseStepModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseStepRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketRepository;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeStageRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock private CaseTicketRepository caseTicketRepository;
    @Mock private CaseStepRepository caseStepRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private CaseTypeRepository caseTypeRepository;
    @Mock private CaseTypeStageRepository caseTypeStageRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private CaseService caseService;

    @BeforeEach
    void setUpSecurityContext() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("admin_user", null, List.of());
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    // ── createCase ────────────────────────────────────────────────────────────

    @Test
    void createCase_savesTicketAndSteps() throws Exception {
        CreateCaseDTO dto = buildCreateDTO(1L, 2L, "Error en login", LocalDate.now().plusDays(10));

        ProjectModel project = buildProject(1L, "Proyecto A");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTypeStageModel stage1 = buildStage(100L, caseType, "Análisis", 1);
        CaseTypeStageModel stage2 = buildStage(101L, caseType, "Desarrollo", 2);

        CaseTicketModel savedTicket = buildTicket(1L, project, caseType, employee,
                "Error en login", CaseStatusEnum.OPEN, dto.getDueDate());

        CaseStepModel step1 = buildStep(200L, savedTicket, stage1, 1, CaseStepStatusEnum.PENDING);
        CaseStepModel step2 = buildStep(201L, savedTicket, stage2, 2, CaseStepStatusEnum.PENDING);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(caseTypeRepository.findById(2L)).thenReturn(Optional.of(caseType));
        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.of(employee));
        when(caseTypeStageRepository.findByCaseTypeIdOrderByStageOrderAsc(2L))
                .thenReturn(List.of(stage1, stage2));
        when(caseTicketRepository.save(any(CaseTicketModel.class))).thenReturn(savedTicket);
        when(caseStepRepository.saveAll(anyList())).thenReturn(List.of(step1, step2));

        CaseDTO result = caseService.createCase(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Error en login", result.title());
        assertEquals("OPEN", result.status());
        assertEquals(2, result.steps().size());
        verify(caseTicketRepository).save(any(CaseTicketModel.class));
        verify(caseStepRepository).saveAll(anyList());
    }

    @Test
    void createCase_projectNotFound_throwsNotFoundException() throws Exception {
        CreateCaseDTO dto = buildCreateDTO(99L, 2L, "Title", LocalDate.now().plusDays(5));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseService.createCase(dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void createCase_caseTypeNotFound_throwsNotFoundException() throws Exception {
        CreateCaseDTO dto = buildCreateDTO(1L, 99L, "Title", LocalDate.now().plusDays(5));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(buildProject(1L, "P")));
        when(caseTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseService.createCase(dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void createCase_noStages_throwsBadRequestException() throws Exception {
        CreateCaseDTO dto = buildCreateDTO(1L, 2L, "Title", LocalDate.now().plusDays(5));
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(buildProject(1L, "P")));
        when(caseTypeRepository.findById(2L)).thenReturn(Optional.of(buildCaseType(2L, "Bug")));
        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.of(employee));
        when(caseTypeStageRepository.findByCaseTypeIdOrderByStageOrderAsc(2L)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> caseService.createCase(dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void createCase_employeeNotFound_throwsBadRequestException() throws Exception {
        CreateCaseDTO dto = buildCreateDTO(1L, 2L, "Title", LocalDate.now().plusDays(5));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(buildProject(1L, "P")));
        when(caseTypeRepository.findById(2L)).thenReturn(Optional.of(buildCaseType(2L, "Bug")));
        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> caseService.createCase(dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void createCase_stepsCreatedWithCorrectOrder() throws Exception {
        CreateCaseDTO dto = buildCreateDTO(1L, 2L, "Title", LocalDate.now().plusDays(5));
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTypeStageModel s1 = buildStage(100L, caseType, "Stage 1", 1);
        CaseTypeStageModel s2 = buildStage(101L, caseType, "Stage 2", 2);
        CaseTypeStageModel s3 = buildStage(102L, caseType, "Stage 3", 3);

        CaseTicketModel saved = buildTicket(1L, project, caseType, employee, "Title",
                CaseStatusEnum.OPEN, dto.getDueDate());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(caseTypeRepository.findById(2L)).thenReturn(Optional.of(caseType));
        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.of(employee));
        when(caseTypeStageRepository.findByCaseTypeIdOrderByStageOrderAsc(2L))
                .thenReturn(List.of(s1, s2, s3));
        when(caseTicketRepository.save(any())).thenReturn(saved);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CaseStepModel>> stepsCaptor = ArgumentCaptor.forClass(List.class);
        CaseStepModel step1 = buildStep(200L, saved, s1, 1, CaseStepStatusEnum.PENDING);
        CaseStepModel step2 = buildStep(201L, saved, s2, 2, CaseStepStatusEnum.PENDING);
        CaseStepModel step3 = buildStep(202L, saved, s3, 3, CaseStepStatusEnum.PENDING);
        when(caseStepRepository.saveAll(stepsCaptor.capture())).thenReturn(List.of(step1, step2, step3));

        caseService.createCase(dto);

        List<CaseStepModel> captured = stepsCaptor.getValue();
        assertEquals(3, captured.size());
        assertEquals(1, captured.get(0).getStepOrder());
        assertEquals(2, captured.get(1).getStepOrder());
        assertEquals(3, captured.get(2).getStepOrder());
    }

    // ── getCases ──────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getCases_returnsPagedResult() throws Exception {
        ProjectModel project = buildProject(1L, "Proyecto A");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Title", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));

        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of(ticket));

        when(caseTicketRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        PagedResponseDTO<CaseSummaryDTO> result = caseService.getCases(new CaseFilterDTO());

        assertEquals(1, result.content().size());
        assertEquals("Title", result.content().get(0).title());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_emptyPage_returnsEmpty() throws Exception {
        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of());
        when(caseTicketRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        PagedResponseDTO<CaseSummaryDTO> result = caseService.getCases(new CaseFilterDTO());

        assertTrue(result.content().isEmpty());
        verify(caseStepRepository, never()).findByCaseTicketIdOrderByStepOrderAsc(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_invalidStatus_throwsBadRequestException() throws Exception {
        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setStatus("INVALID_STATUS");

        assertThrows(BadRequestException.class, () -> caseService.getCases(filter));
        verify(caseTicketRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_paginationParams_passedCorrectly() throws Exception {
        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(caseTicketRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(mockPage);

        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setPage(2);
        filter.setSize(20);

        caseService.getCases(filter);

        assertEquals(2, pageableCaptor.getValue().getPageNumber());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_sizeExceedsMax_cappedAt100() {
        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(caseTicketRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(mockPage);

        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setSize(999);

        caseService.getCases(filter);

        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_pageNegative_normalizedToZero() throws Exception {
        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(caseTicketRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(mockPage);

        CaseFilterDTO filter = new CaseFilterDTO();
        filter.setPage(-5);

        caseService.getCases(filter);

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_overdueTicket_markedAsOverdue() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Expired", CaseStatusEnum.OPEN, LocalDate.now().minusDays(1));

        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of(ticket));
        when(caseTicketRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        PagedResponseDTO<CaseSummaryDTO> result = caseService.getCases(new CaseFilterDTO());

        assertTrue(result.content().get(0).overdue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCases_completedTicket_notMarkedOverdue() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Done", CaseStatusEnum.COMPLETED, LocalDate.now().minusDays(1));

        Page<CaseTicketModel> mockPage = new PageImpl<>(List.of(ticket));
        when(caseTicketRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        PagedResponseDTO<CaseSummaryDTO> result = caseService.getCases(new CaseFilterDTO());

        assertFalse(result.content().get(0).overdue());
    }

    // ── getCasesByProject ─────────────────────────────────────────────────────

    @Test
    void getCasesByProject_returnsTicketsForProject() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel t1 = buildTicket(1L, project, caseType, employee,
                "T1", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTicketModel t2 = buildTicket(2L, project, caseType, employee,
                "T2", CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(3));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(caseTicketRepository.findByProjectId(1L)).thenReturn(List.of(t1, t2));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(anyLong())).thenReturn(List.of());

        List<CaseSummaryDTO> result = caseService.getCasesByProject(1L);

        assertEquals(2, result.size());
        assertEquals("T1", result.get(0).title());
        assertEquals("T2", result.get(1).title());
    }

    @Test
    void getCasesByProject_projectNotFound_throwsNotFoundException() throws Exception {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseService.getCasesByProject(99L));
        verify(caseTicketRepository, never()).findByProjectId(any());
    }

    @Test
    void getCasesByProject_noCases_returnsEmptyList() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(buildProject(1L, "P")));
        when(caseTicketRepository.findByProjectId(1L)).thenReturn(List.of());

        List<CaseSummaryDTO> result = caseService.getCasesByProject(1L);

        assertTrue(result.isEmpty());
    }

    // ── getCaseById ───────────────────────────────────────────────────────────

    @Test
    void getCaseById_found_returnsDTOWithSteps() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.PENDING);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));

        CaseDTO result = caseService.getCaseById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Case", result.title());
        assertEquals(1, result.steps().size());
        assertEquals("Stage", result.steps().get(0).stageName());
    }

    @Test
    void getCaseById_notFound_throwsNotFoundException() throws Exception {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> caseService.getCaseById(99L));
    }

    @Test
    void getCaseById_progressCalculated_allApproved() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.APPROVED);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));

        CaseDTO result = caseService.getCaseById(1L);

        assertEquals(100, result.progressPercent());
    }

    @Test
    void getCaseById_progressCalculated_noneApproved() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage1 = buildStage(100L, caseType, "S1", 1);
        CaseTypeStageModel stage2 = buildStage(101L, caseType, "S2", 2);
        CaseStepModel step1 = buildStep(200L, ticket, stage1, 1, CaseStepStatusEnum.PENDING);
        CaseStepModel step2 = buildStep(201L, ticket, stage2, 2, CaseStepStatusEnum.IN_PROGRESS);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step1, step2));

        CaseDTO result = caseService.getCaseById(1L);

        assertEquals(0, result.progressPercent());
    }

    @Test
    void getCaseById_stepWithAssignedEmployee_returnsAssignedInfo() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel creator = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        EmployeeModel assignee = buildEmployee(11L, "Ana", "García", "ana");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, creator,
                "Case", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.ASSIGNED);
        step.setAssignedEmployee(assignee);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of(step));

        CaseDTO result = caseService.getCaseById(1L);

        assertEquals(11L, result.steps().get(0).assignedEmployeeId());
        assertEquals("Ana García", result.steps().get(0).assignedEmployeeName());
    }

    // ── updateDueDate ─────────────────────────────────────────────────────────

    @Test
    void updateDueDate_updatesAndReturnsCaseDTO() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(20));

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        CaseDTO result = caseService.updateDueDate(1L, dto);

        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(20), result.dueDate());
        verify(caseTicketRepository).save(ticket);
    }

    @Test
    void updateDueDate_canceledCase_throwsBadRequestException() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.CANCELED, LocalDate.now().plusDays(5));

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(20));

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class, () -> caseService.updateDueDate(1L, dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void updateDueDate_completedCase_throwsBadRequestException() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.COMPLETED, LocalDate.now().plusDays(5));

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(20));

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class, () -> caseService.updateDueDate(1L, dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void updateDueDate_notFound_throwsNotFoundException() throws Exception {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(10));

        assertThrows(NotFoundException.class, () -> caseService.updateDueDate(99L, dto));
    }

    // ── cancelCase ────────────────────────────────────────────────────────────

    @Test
    void cancelCase_openCase_cancelsSuccessfully() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Cliente canceló el requerimiento");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        CaseDTO result = caseService.cancelCase(1L, dto);

        assertNotNull(result);
        assertEquals("CANCELED", result.status());
        assertNotNull(result.cancelReason());
        verify(caseTicketRepository).save(ticket);
    }

    @Test
    void cancelCase_inProgressCase_cancelsSuccessfully() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Razón válida");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        CaseDTO result = caseService.cancelCase(1L, dto);

        assertEquals("CANCELED", result.status());
    }

    @Test
    void cancelCase_alreadyCanceled_throwsBadRequestException() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.CANCELED, LocalDate.now().plusDays(5));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Razón");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class, () -> caseService.cancelCase(1L, dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void cancelCase_completedCase_throwsBadRequestException() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.COMPLETED, LocalDate.now().plusDays(5));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Razón");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class, () -> caseService.cancelCase(1L, dto));
        verify(caseTicketRepository, never()).save(any());
    }

    @Test
    void cancelCase_notFound_throwsNotFoundException() throws Exception {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Razón");

        assertThrows(NotFoundException.class, () -> caseService.cancelCase(99L, dto));
    }

    @Test
    void cancelCase_setsCanceledAtAndReason() throws Exception {
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Case", CaseStatusEnum.OPEN, LocalDate.now().plusDays(5));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("Mi razón");

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        caseService.cancelCase(1L, dto);

        assertNotNull(ticket.getCanceledAt());
        assertEquals("Mi razón", ticket.getCancelReason());
    }

    // ── getMyAssignedCases ────────────────────────────────────────────────────

    @Test
    void getMyAssignedCases_returnsCasesWhereEmployeeHasSteps() {
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        CaseTicketModel ticket1 = buildTicket(1L, project, caseType, employee,
                "Caso A", CaseStatusEnum.IN_PROGRESS, LocalDate.now().plusDays(5));
        CaseTicketModel ticket2 = buildTicket(2L, project, caseType, employee,
                "Caso B", CaseStatusEnum.OPEN, LocalDate.now().plusDays(10));

        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.of(employee));
        when(caseStepRepository.findDistinctCaseIdsByAssignedEmployeeId(10L))
                .thenReturn(List.of(1L, 2L));
        when(caseTicketRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(ticket1, ticket2));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(anyLong()))
                .thenReturn(List.of());

        List<CaseSummaryDTO> result = caseService.getMyAssignedCases();

        assertEquals(2, result.size());
        assertEquals("Caso A", result.get(0).title());
        assertEquals("Caso B", result.get(1).title());
    }

    @Test
    void getMyAssignedCases_noAssignedSteps_returnsEmptyList() {
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");

        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.of(employee));
        when(caseStepRepository.findDistinctCaseIdsByAssignedEmployeeId(10L))
                .thenReturn(List.of());

        List<CaseSummaryDTO> result = caseService.getMyAssignedCases();

        assertTrue(result.isEmpty());
        verify(caseTicketRepository, never()).findAllById(any());
    }

    @Test
    void getMyAssignedCases_includesProgressAndOverdueMeta() {
        EmployeeModel employee = buildEmployee(10L, "Juan", "Pérez", "admin_user");
        ProjectModel project = buildProject(1L, "P");
        CaseTypeModel caseType = buildCaseType(2L, "Bug");
        CaseTicketModel ticket = buildTicket(1L, project, caseType, employee,
                "Overdue", CaseStatusEnum.IN_PROGRESS, LocalDate.now().minusDays(1));
        CaseTypeStageModel stage = buildStage(100L, caseType, "Stage", 1);
        CaseStepModel step = buildStep(200L, ticket, stage, 1, CaseStepStatusEnum.APPROVED);

        when(employeeRepository.findByUserUsername("admin_user")).thenReturn(Optional.of(employee));
        when(caseStepRepository.findDistinctCaseIdsByAssignedEmployeeId(10L))
                .thenReturn(List.of(1L));
        when(caseTicketRepository.findAllById(List.of(1L))).thenReturn(List.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L))
                .thenReturn(List.of(step));

        List<CaseSummaryDTO> result = caseService.getMyAssignedCases();

        assertEquals(1, result.size());
        assertTrue(result.get(0).overdue());
        assertEquals(100, result.get(0).progressPercent());
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static CreateCaseDTO buildCreateDTO(Long projectId, Long caseTypeId,
                                                 String title, LocalDate dueDate) {
        CreateCaseDTO dto = new CreateCaseDTO();
        dto.setProjectId(projectId);
        dto.setCaseTypeId(caseTypeId);
        dto.setTitle(title);
        dto.setDueDate(dueDate);
        return dto;
    }

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

    private static EmployeeModel buildEmployee(Long id, String firstName, String lastName,
                                                String username) {
        UserModel user = new UserModel();
        user.setUsername(username);

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
                                                String title, CaseStatusEnum status,
                                                LocalDate dueDate) {
        CaseTicketModel t = new CaseTicketModel();
        t.setId(id);
        t.setProject(project);
        t.setCaseType(caseType);
        t.setCreatedByEmployee(employee);
        t.setTitle(title);
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
