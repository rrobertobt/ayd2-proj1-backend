package edu.robertob.ayd2_p1_backend.cases.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CancelCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.UpdateDueDateDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseStepRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketRepository;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeStageRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectAdminAssignmentRepository;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class CaseServiceTest {

    @Mock CaseTicketRepository caseTicketRepository;
    @Mock CaseStepRepository caseStepRepository;
    @Mock ProjectRepository projectRepository;
    @Mock ProjectAdminAssignmentRepository projectAdminAssignmentRepository;
    @Mock CaseTypeRepository caseTypeRepository;
    @Mock CaseTypeStageRepository caseTypeStageRepository;
    @Mock EmployeeRepository employeeRepository;

    @InjectMocks CaseService service;

    private Authentication auth;

    @BeforeEach
    void setUpSecurity() {
        SecurityContext ctx = mock(SecurityContext.class);
        auth = mock(Authentication.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn("user");
        lenient().when(auth.getAuthorities()).thenAnswer(inv -> List.of());
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void clearSecurity() { SecurityContextHolder.clearContext(); }

    // ── createCase ────────────────────────────────────────────────────────────

    @Test
    void createCase_throwsWhenProjectNotFound() {
        CreateCaseDTO dto = buildCreateCaseDto(1L, 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createCase(dto));
    }

    @Test
    void createCase_throwsWhenProjectInactive() {
        CreateCaseDTO dto = buildCreateCaseDto(1L, 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(buildProject(ProjectStatusEnum.INACTIVE)));

        assertThrows(BadRequestException.class, () -> service.createCase(dto));
    }

    @Test
    void createCase_throwsWhenNoStagesDefined() throws NotFoundException {
        CreateCaseDTO dto = buildCreateCaseDto(1L, 1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(buildProject(ProjectStatusEnum.ACTIVE)));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(buildCaseType()));
        when(employeeRepository.findByUserUsername("user")).thenReturn(Optional.of(buildEmployee()));
        when(caseTypeStageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> service.createCase(dto));
    }

    @Test
    void createCase_success() throws NotFoundException {
        CreateCaseDTO dto = buildCreateCaseDto(1L, 1L);
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTypeModel caseType = buildCaseType();
        EmployeeModel employee = buildEmployee();
        CaseTypeStageModel stage = buildStage(caseType);
        CaseTicketModel saved = buildTicket(project, caseType, employee);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(caseType));
        when(employeeRepository.findByUserUsername("user")).thenReturn(Optional.of(employee));
        when(caseTypeStageRepository.findByCaseTypeIdOrderByStageOrderAsc(1L)).thenReturn(List.of(stage));
        when(caseTicketRepository.save(any())).thenReturn(saved);
        when(caseStepRepository.saveAll(any())).thenReturn(List.of());

        var result = service.createCase(dto);

        assertNotNull(result);
        assertEquals("OPEN", result.status());
        verify(caseTicketRepository).save(any());
        verify(caseStepRepository).saveAll(any());
    }

    // ── getCaseById ───────────────────────────────────────────────────────────

    @Test
    void getCaseById_throwsWhenNotFound() {
        when(caseTicketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getCaseById(99L));
    }

    @Test
    void getCaseById_returnsDTO() throws NotFoundException {
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTypeModel caseType = buildCaseType();
        EmployeeModel employee = buildEmployee();
        CaseTicketModel ticket = buildTicket(project, caseType, employee);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        var result = service.getCaseById(1L);

        assertNotNull(result);
        assertEquals(ticket.getId(), result.id());
    }

    // ── updateDueDate ─────────────────────────────────────────────────────────

    @Test
    void updateDueDate_throwsWhenProjectInactive() {
        ProjectModel project = buildProject(ProjectStatusEnum.INACTIVE);
        CaseTicketModel ticket = buildTicket(project, buildCaseType(), buildEmployee());
        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(5));
        assertThrows(BadRequestException.class, () -> service.updateDueDate(1L, dto));
    }

    @Test
    void updateDueDate_throwsWhenCanceled() {
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTicketModel ticket = buildTicket(project, buildCaseType(), buildEmployee());
        ticket.setStatus(CaseStatusEnum.CANCELED);
        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(LocalDate.now().plusDays(5));
        assertThrows(BadRequestException.class, () -> service.updateDueDate(1L, dto));
    }

    @Test
    void updateDueDate_success() throws NotFoundException {
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTicketModel ticket = buildTicket(project, buildCaseType(), buildEmployee());
        LocalDate newDate = LocalDate.now().plusDays(10);

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        UpdateDueDateDTO dto = new UpdateDueDateDTO();
        dto.setDueDate(newDate);
        var result = service.updateDueDate(1L, dto);

        assertEquals(newDate, result.dueDate());
        verify(caseTicketRepository).save(ticket);
    }

    // ── cancelCase ────────────────────────────────────────────────────────────

    @Test
    void cancelCase_throwsWhenAlreadyCanceled() {
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTicketModel ticket = buildTicket(project, buildCaseType(), buildEmployee());
        ticket.setStatus(CaseStatusEnum.CANCELED);
        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("duplicate");
        assertThrows(BadRequestException.class, () -> service.cancelCase(1L, dto));
    }

    @Test
    void cancelCase_throwsWhenCompleted() {
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTicketModel ticket = buildTicket(project, buildCaseType(), buildEmployee());
        ticket.setStatus(CaseStatusEnum.COMPLETED);
        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("done");
        assertThrows(BadRequestException.class, () -> service.cancelCase(1L, dto));
    }

    @Test
    void cancelCase_success() throws NotFoundException {
        ProjectModel project = buildProject(ProjectStatusEnum.ACTIVE);
        CaseTicketModel ticket = buildTicket(project, buildCaseType(), buildEmployee());

        when(caseTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(caseTicketRepository.save(ticket)).thenReturn(ticket);
        when(caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(1L)).thenReturn(List.of());

        CancelCaseDTO dto = new CancelCaseDTO();
        dto.setReason("no longer needed");
        var result = service.cancelCase(1L, dto);

        assertEquals("CANCELED", result.status());
        assertEquals("no longer needed", result.cancelReason());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ProjectModel buildProject(ProjectStatusEnum status) {
        ProjectModel p = new ProjectModel();
        p.setId(1L);
        p.setName("Test Project");
        p.setStatus(status);
        return p;
    }

    private CaseTypeModel buildCaseType() {
        CaseTypeModel ct = new CaseTypeModel();
        ct.setId(1L);
        ct.setName("Bug");
        return ct;
    }

    private CaseTypeStageModel buildStage(CaseTypeModel caseType) {
        CaseTypeStageModel s = new CaseTypeStageModel();
        s.setId(1L);
        s.setCaseType(caseType);
        s.setName("Analysis");
        s.setStageOrder(1);
        return s;
    }

    private EmployeeModel buildEmployee() {
        EmployeeModel e = new EmployeeModel();
        e.setId(1L);
        e.setFirst_name("John");
        e.setLast_name("Doe");
        return e;
    }

    private CaseTicketModel buildTicket(ProjectModel project, CaseTypeModel caseType, EmployeeModel employee) {
        CaseTicketModel t = new CaseTicketModel();
        t.setId(1L);
        t.setProject(project);
        t.setCaseType(caseType);
        t.setCreatedByEmployee(employee);
        t.setTitle("Test Case");
        t.setStatus(CaseStatusEnum.OPEN);
        t.setDueDate(LocalDate.now().plusDays(7));
        return t;
    }

    private CreateCaseDTO buildCreateCaseDto(Long projectId, Long caseTypeId) {
        CreateCaseDTO dto = new CreateCaseDTO();
        dto.setProjectId(projectId);
        dto.setCaseTypeId(caseTypeId);
        dto.setTitle("New Case");
        dto.setDueDate(LocalDate.now().plusDays(7));
        return dto;
    }
}
