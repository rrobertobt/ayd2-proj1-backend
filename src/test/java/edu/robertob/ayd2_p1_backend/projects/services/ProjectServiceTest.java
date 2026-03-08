package edu.robertob.ayd2_p1_backend.projects.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.AssignProjectAdminDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.CreateProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.ProjectFilterDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.UpdateProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.response.ProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectAdminAssignmentModel;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectAdminAssignmentRepository;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectAdminAssignmentRepository assignmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ProjectService projectService;

    // ── createProject ─────────────────────────────────────────────────────────

    @Test
    void createProject_savesProjectWithActiveStatusAndReturnsDTO() {
        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("Sistema ERP");
        dto.setDescription("Proyecto ERP empresa X");

        ProjectModel saved = buildProject(1L, "Sistema ERP", "Proyecto ERP empresa X", ProjectStatusEnum.ACTIVE);
        when(projectRepository.save(any(ProjectModel.class))).thenReturn(saved);

        ProjectDTO result = projectService.createProject(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Sistema ERP", result.name());
        assertEquals("Proyecto ERP empresa X", result.description());
        assertEquals("ACTIVE", result.status());
        assertNull(result.currentAdmin());
        verify(projectRepository).save(any(ProjectModel.class));
    }

    @Test
    void createProject_setsStatusToActive() {
        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("Proyecto Test");
        dto.setDescription(null);

        ArgumentCaptor<ProjectModel> captor = ArgumentCaptor.forClass(ProjectModel.class);
        ProjectModel saved = buildProject(2L, "Proyecto Test", null, ProjectStatusEnum.ACTIVE);
        when(projectRepository.save(captor.capture())).thenReturn(saved);

        projectService.createProject(dto);

        assertEquals(ProjectStatusEnum.ACTIVE, captor.getValue().getStatus());
    }

    // ── getProjects ───────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_noFilter_returnsPagedResult() {
        ProjectModel p1 = buildProject(1L, "P1", "d1", ProjectStatusEnum.ACTIVE);
        ProjectModel p2 = buildProject(2L, "P2", "d2", ProjectStatusEnum.INACTIVE);
        Page<ProjectModel> mockPage = new PageImpl<>(List.of(p1, p2));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(assignmentRepository.findByProjectIdAndActiveTrue(anyLong())).thenReturn(Optional.empty());

        ProjectFilterDTO filter = new ProjectFilterDTO();
        PagedResponseDTO<ProjectDTO> result = projectService.getProjects(filter);

        assertEquals(2, result.content().size());
        verify(projectRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_withStatusFilter_appliesFilter() {
        ProjectModel p1 = buildProject(1L, "P1", "d1", ProjectStatusEnum.ACTIVE);
        Page<ProjectModel> mockPage = new PageImpl<>(List.of(p1));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("ACTIVE");

        PagedResponseDTO<ProjectDTO> result = projectService.getProjects(filter);

        assertEquals(1, result.content().size());
        assertEquals("ACTIVE", result.content().get(0).status());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_withLowercaseStatusFilter_works() {
        Page<ProjectModel> mockPage = new PageImpl<>(List.of());
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("inactive");

        PagedResponseDTO<ProjectDTO> result = projectService.getProjects(filter);

        assertTrue(result.content().isEmpty());
    }

    @Test
    void getProjects_withInvalidStatusFilter_throwsBadRequestException() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("UNKNOWN");

        assertThrows(BadRequestException.class, () -> projectService.getProjects(filter));
        verify(projectRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_withSearchFilter_passesSpecification() {
        Page<ProjectModel> mockPage = new PageImpl<>(List.of());
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setSearch("erp");

        projectService.getProjects(filter);

        verify(projectRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_paginationParams_passedCorrectly() {
        Page<ProjectModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(mockPage);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setPage(2);
        filter.setSize(5);
        filter.setSortBy("name");
        filter.setSortDir("asc");

        projectService.getProjects(filter);

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_sizeExceedsMax_cappedAt100() {
        Page<ProjectModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(mockPage);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setSize(999);

        projectService.getProjects(filter);

        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_pageNegative_normalizedToZero() {
        Page<ProjectModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(mockPage);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setPage(-5);

        projectService.getProjects(filter);

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_withAdmin_includesAdminInfoInResult() {
        ProjectModel p = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        EmployeeModel emp = buildEmployee(10L, "John", "Doe");
        ProjectAdminAssignmentModel assignment = buildAssignment(5L, p, emp, true);
        Page<ProjectModel> mockPage = new PageImpl<>(List.of(p));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(assignment));

        PagedResponseDTO<ProjectDTO> result = projectService.getProjects(new ProjectFilterDTO());

        assertEquals(1, result.content().size());
        assertNotNull(result.content().get(0).currentAdmin());
        assertEquals(5L, result.content().get(0).currentAdmin().assignmentId());
        assertEquals("John", result.content().get(0).currentAdmin().firstName());
        assertEquals("Doe", result.content().get(0).currentAdmin().lastName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_unknownSortBy_defaultsToCreatedAt() {
        Page<ProjectModel> mockPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(mockPage);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setSortBy("nonExistentField");

        projectService.getProjects(filter);

        // Should not throw; defaults to createdAt
        verify(projectRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_returnsCorrectPageMetadata() {
        ProjectModel p = buildProject(1L, "P", "d", ProjectStatusEnum.ACTIVE);
        Page<ProjectModel> mockPage = new PageImpl<>(List.of(p),
                org.springframework.data.domain.PageRequest.of(0, 10), 1L);
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(assignmentRepository.findByProjectIdAndActiveTrue(anyLong())).thenReturn(Optional.empty());

        PagedResponseDTO<ProjectDTO> result = projectService.getProjects(new ProjectFilterDTO());

        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
    }

    // ── getProjectById ────────────────────────────────────────────────────────

    @Test
    void getProjectById_found_returnsProjectDTO() throws NotFoundException {
        ProjectModel p = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.getProjectById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("P1", result.name());
    }

    @Test
    void getProjectById_notFound_throwsNotFoundException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> projectService.getProjectById(99L));
    }

    @Test
    void getProjectById_withAdmin_includesAdminInfo() throws NotFoundException {
        ProjectModel p = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        EmployeeModel emp = buildEmployee(7L, "Alice", "Smith");
        ProjectAdminAssignmentModel assignment = buildAssignment(3L, p, emp, true);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(assignment));

        ProjectDTO result = projectService.getProjectById(1L);

        assertNotNull(result.currentAdmin());
        assertEquals(7L, result.currentAdmin().employeeId());
        assertEquals("Alice", result.currentAdmin().firstName());
    }

    // ── updateProject ─────────────────────────────────────────────────────────

    @Test
    void updateProject_updatesNameAndDescription() throws NotFoundException {
        ProjectModel p = buildProject(1L, "Old Name", "Old Desc", ProjectStatusEnum.ACTIVE);
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("New Name");
        dto.setDescription("New Desc");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.updateProject(1L, dto);

        assertEquals("New Name", result.name());
        assertEquals("New Desc", result.description());
        verify(projectRepository).save(p);
    }

    @Test
    void updateProject_updatesNameOnly_descriptionUnchanged() throws NotFoundException {
        ProjectModel p = buildProject(1L, "Old Name", "Keep Desc", ProjectStatusEnum.ACTIVE);
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("New Name");
        dto.setDescription(null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.updateProject(1L, dto);

        assertEquals("New Name", result.name());
        assertEquals("Keep Desc", result.description());
    }

    @Test
    void updateProject_emptyName_doesNotUpdateName() throws NotFoundException {
        ProjectModel p = buildProject(1L, "Keep Name", "Old Desc", ProjectStatusEnum.ACTIVE);
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("");
        dto.setDescription("New Desc");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        projectService.updateProject(1L, dto);

        assertEquals("Keep Name", p.getName());
    }

    @Test
    void updateProject_projectNotFound_throwsNotFoundException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        UpdateProjectDTO dto = new UpdateProjectDTO();

        assertThrows(NotFoundException.class, () -> projectService.updateProject(99L, dto));
        verify(projectRepository, never()).save(any());
    }

    // ── toggleStatus ──────────────────────────────────────────────────────────

    @Test
    void toggleStatus_activeToInactive() throws NotFoundException {
        ProjectModel p = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.toggleStatus(1L);

        assertEquals("INACTIVE", result.status());
        assertEquals(ProjectStatusEnum.INACTIVE, p.getStatus());
    }

    @Test
    void toggleStatus_inactiveToActive() throws NotFoundException {
        ProjectModel p = buildProject(2L, "P2", "desc", ProjectStatusEnum.INACTIVE);
        when(projectRepository.findById(2L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(2L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.toggleStatus(2L);

        assertEquals("ACTIVE", result.status());
        assertEquals(ProjectStatusEnum.ACTIVE, p.getStatus());
    }

    @Test
    void toggleStatus_projectNotFound_throwsNotFoundException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> projectService.toggleStatus(99L));
    }

    // ── assignAdmin ───────────────────────────────────────────────────────────

    @Test
    void assignAdmin_validProjectAdminUser_createsNewAssignment() throws NotFoundException {
        ProjectModel project = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        RoleModel role = buildRole(2L, RolesEnum.PROJECT_ADMIN);
        UserModel user = buildUser(20L, role);
        EmployeeModel employee = buildEmployee(10L, "Bob", "Martin");

        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(20L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(20L)).thenReturn(Optional.of(employee));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any(ProjectAdminAssignmentModel.class)))
                .thenAnswer(inv -> {
                    ProjectAdminAssignmentModel a = inv.getArgument(0);
                    a.setId(99L);
                    return a;
                });

        ProjectDTO result = projectService.assignAdmin(1L, dto);

        assertNotNull(result.currentAdmin());
        assertEquals(10L, result.currentAdmin().employeeId());
        assertEquals("Bob", result.currentAdmin().firstName());
        verify(assignmentRepository).save(any(ProjectAdminAssignmentModel.class));
    }

    @Test
    void assignAdmin_existingActiveAssignment_deactivatesOldAndCreatesNew() throws NotFoundException {
        ProjectModel project = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        RoleModel role = buildRole(2L, RolesEnum.PROJECT_ADMIN);
        UserModel user = buildUser(20L, role);
        EmployeeModel employee = buildEmployee(10L, "Bob", "Martin");

        EmployeeModel previousEmp = buildEmployee(5L, "Previous", "Admin");
        ProjectAdminAssignmentModel existingAssignment = buildAssignment(50L, project, previousEmp, true);

        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(20L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(20L)).thenReturn(Optional.of(employee));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(existingAssignment));
        when(assignmentRepository.save(any(ProjectAdminAssignmentModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        projectService.assignAdmin(1L, dto);

        assertFalse(existingAssignment.isActive());
        assertNotNull(existingAssignment.getEndDate());
        verify(assignmentRepository, times(2)).save(any(ProjectAdminAssignmentModel.class));
    }

    @Test
    void assignAdmin_projectNotFound_throwsNotFoundException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(1L);

        assertThrows(NotFoundException.class, () -> projectService.assignAdmin(99L, dto));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void assignAdmin_userNotFound_throwsNotFoundException() {
        ProjectModel project = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(99L);

        assertThrows(NotFoundException.class, () -> projectService.assignAdmin(1L, dto));
    }

    @Test
    void assignAdmin_userNotProjectAdmin_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        RoleModel role = buildRole(1L, RolesEnum.DEVELOPER);
        UserModel user = buildUser(20L, role);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));

        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(20L);

        assertThrows(BadRequestException.class, () -> projectService.assignAdmin(1L, dto));
        verify(employeeRepository, never()).findByUserId(any());
    }

    @Test
    void assignAdmin_userIsSystemAdmin_throwsBadRequestException() {
        ProjectModel project = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        RoleModel role = buildRole(1L, RolesEnum.SYSTEM_ADMIN);
        UserModel user = buildUser(20L, role);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));

        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(20L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> projectService.assignAdmin(1L, dto));
        assertTrue(ex.getMessage().contains("PROJECT_ADMIN"));
    }

    @Test
    void assignAdmin_employeeNotFound_throwsNotFoundException() {
        ProjectModel project = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        RoleModel role = buildRole(2L, RolesEnum.PROJECT_ADMIN);
        UserModel user = buildUser(20L, role);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(employeeRepository.findByUserId(20L)).thenReturn(Optional.empty());

        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(20L);

        assertThrows(NotFoundException.class, () -> projectService.assignAdmin(1L, dto));
        verify(assignmentRepository, never()).save(any());
    }

    // ── toDTO helper coverage ─────────────────────────────────────────────────

    @Test
    void getProjectById_noAdmin_adminInfoIsNull() throws NotFoundException {
        ProjectModel p = buildProject(1L, "P1", "desc", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.getProjectById(1L);

        assertNull(result.currentAdmin());
    }

    // ── builder helpers ───────────────────────────────────────────────────────

    private static ProjectModel buildProject(Long id, String name, String description, ProjectStatusEnum status) {
        ProjectModel project = new ProjectModel();
        project.setId(id);
        project.setName(name);
        project.setDescription(description);
        project.setStatus(status);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        return project;
    }

    private static EmployeeModel buildEmployee(Long id, String firstName, String lastName) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setFirst_name(firstName);
        emp.setLast_name(lastName);
        emp.setHourly_rate(0.0);
        return emp;
    }

    private static RoleModel buildRole(Long id, RolesEnum code) {
        return new RoleModel(id, code, code.getCode(), null);
    }

    private static UserModel buildUser(Long id, RoleModel role) {
        return new UserModel(id, "user" + id, "user" + id + "@mail.com", "hash", role);
    }

    private static ProjectAdminAssignmentModel buildAssignment(Long id, ProjectModel project,
                                                                EmployeeModel employee, boolean active) {
        ProjectAdminAssignmentModel a = new ProjectAdminAssignmentModel();
        a.setId(id);
        a.setProject(project);
        a.setEmployee(employee);
        a.setActive(active);
        return a;
    }
}
