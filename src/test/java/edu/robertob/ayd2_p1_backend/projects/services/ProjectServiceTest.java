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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

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
    @InjectMocks private ProjectService projectService;

    @BeforeEach
    void setUpAuth() {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, List.of());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @AfterEach
    void clearAuth() { SecurityContextHolder.clearContext(); }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static ProjectModel proj(Long id, String name, ProjectStatusEnum st) {
        ProjectModel p = new ProjectModel();
        p.setId(id); p.setName(name); p.setStatus(st); p.setDescription("desc");
        return p;
    }

    private static EmployeeModel emp(Long id) {
        EmployeeModel e = new EmployeeModel();
        e.setId(id); e.setFirst_name("John"); e.setLast_name("Doe"); e.setHourly_rate(10.0);
        return e;
    }

    private static UserModel user(Long id, RolesEnum code) {
        UserModel u = new UserModel();
        u.setId(id); u.setUsername("user" + id);
        u.setRole(new RoleModel(1L, code, code.getCode(), null));
        return u;
    }

    private static ProjectAdminAssignmentModel assign(Long id, ProjectModel p, EmployeeModel e) {
        ProjectAdminAssignmentModel a = new ProjectAdminAssignmentModel();
        a.setId(id); a.setProject(p); a.setEmployee(e); a.setActive(true);
        return a;
    }

    private void setProjectAdminAuth() {
        var auth = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_PROJECT_ADMIN")));
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // ── createProject ─────────────────────────────────────────────────────────

    @Test
    void createProject_savesAndReturnsDTO() {
        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("P1"); dto.setDescription("D1");
        ProjectModel saved = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        when(projectRepository.save(any())).thenReturn(saved);

        ProjectDTO result = projectService.createProject(dto);

        assertEquals("P1", result.name());
        assertEquals("ACTIVE", result.status());
        assertNull(result.currentAdmin());
    }

    // ── getProjects ───────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getProjects_returnsPagedResult() {
        ProjectFilterDTO f = new ProjectFilterDTO();
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        PagedResponseDTO<ProjectDTO> result = projectService.getProjects(f);

        assertEquals(1, result.content().size());
        assertEquals("P1", result.content().get(0).name());
    }

    @Test
    void getProjects_invalidStatus_throwsBadRequest() {
        ProjectFilterDTO f = new ProjectFilterDTO();
        f.setStatus("INVALID");
        assertThrows(BadRequestException.class, () -> projectService.getProjects(f));
    }

    // ── getProjectById ────────────────────────────────────────────────────────

    @Test
    void getProjectById_found_returnsDTO() throws NotFoundException {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.getProjectById(1L);
        assertEquals(1L, result.id());
        assertNull(result.currentAdmin());
    }

    @Test
    void getProjectById_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.getProjectById(99L));
    }

    @Test
    void getProjectById_projectAdmin_noAssignment_throwsAccessDenied() {
        setProjectAdminAuth();
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        when(employeeRepository.findByUserUsername("admin")).thenReturn(Optional.of(emp(10L)));

        assertThrows(AccessDeniedException.class, () -> projectService.getProjectById(1L));
    }

    @Test
    void getProjectById_projectAdmin_assignedToSelf_returnsDTO() throws NotFoundException {
        setProjectAdminAuth();
        EmployeeModel e = emp(10L);
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        ProjectAdminAssignmentModel a = assign(5L, p, e);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(a));
        when(employeeRepository.findByUserUsername("admin")).thenReturn(Optional.of(e));

        ProjectDTO result = projectService.getProjectById(1L);
        assertNotNull(result.currentAdmin());
        assertEquals(10L, result.currentAdmin().employeeId());
    }

    // ── getMyProjects ─────────────────────────────────────────────────────────

    @Test
    void getMyProjects_asSystemAdmin_returnsAll() {
        EmployeeModel e = emp(1L);
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        when(employeeRepository.findByUserUsername("admin")).thenReturn(Optional.of(e));
        when(projectRepository.findAll()).thenReturn(List.of(p));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        List<ProjectDTO> result = projectService.getMyProjects();
        assertEquals(1, result.size());
    }

    @Test
    void getMyProjects_asProjectAdmin_returnsAssigned() {
        setProjectAdminAuth();
        EmployeeModel e = emp(10L);
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        ProjectAdminAssignmentModel a = assign(5L, p, e);
        when(employeeRepository.findByUserUsername("admin")).thenReturn(Optional.of(e));
        when(assignmentRepository.findByEmployeeIdAndActiveTrue(10L)).thenReturn(List.of(a));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(a));

        List<ProjectDTO> result = projectService.getMyProjects();
        assertEquals(1, result.size());
    }

    // ── updateProject ─────────────────────────────────────────────────────────

    @Test
    void updateProject_updatesFields() throws NotFoundException {
        ProjectModel p = proj(1L, "Old", ProjectStatusEnum.ACTIVE);
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("New"); dto.setDescription("NewDesc");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ProjectDTO result = projectService.updateProject(1L, dto);
        assertEquals("New", result.name());
    }

    @Test
    void updateProject_emptyName_nameUnchanged() throws NotFoundException {
        ProjectModel p = proj(1L, "Keep", ProjectStatusEnum.ACTIVE);
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName(""); dto.setDescription("NewDesc");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        projectService.updateProject(1L, dto);
        assertEquals("Keep", p.getName());
    }

    @Test
    void updateProject_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.updateProject(99L, new UpdateProjectDTO()));
    }

    // ── toggleStatus ──────────────────────────────────────────────────────────

    @Test
    void toggleStatus_activeToInactive() throws NotFoundException {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        projectService.toggleStatus(1L);
        assertEquals(ProjectStatusEnum.INACTIVE, p.getStatus());
    }

    @Test
    void toggleStatus_inactiveToActive() throws NotFoundException {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.INACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(projectRepository.save(p)).thenReturn(p);
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        projectService.toggleStatus(1L);
        assertEquals(ProjectStatusEnum.ACTIVE, p.getStatus());
    }

    @Test
    void toggleStatus_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.toggleStatus(99L));
    }

    // ── assignAdmin ───────────────────────────────────────────────────────────

    @Test
    void assignAdmin_valid_createsAssignment() throws NotFoundException {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        UserModel u = user(2L, RolesEnum.PROJECT_ADMIN);
        EmployeeModel e = emp(10L);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(e));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any())).thenReturn(assign(5L, p, e));

        ProjectDTO result = projectService.assignAdmin(1L, dto);
        assertNotNull(result.currentAdmin());
    }

    @Test
    void assignAdmin_inactiveProject_throwsBadRequest() {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.INACTIVE);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(BadRequestException.class, () -> projectService.assignAdmin(1L, dto));
    }

    @Test
    void assignAdmin_projectNotFound_throws() {
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.assignAdmin(99L, dto));
    }

    @Test
    void assignAdmin_userNotFound_throws() {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(99L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.assignAdmin(1L, dto));
    }

    @Test
    void assignAdmin_wrongRole_throwsBadRequest() {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        UserModel u = user(2L, RolesEnum.DEVELOPER);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        assertThrows(BadRequestException.class, () -> projectService.assignAdmin(1L, dto));
    }

    @Test
    void assignAdmin_employeeNotFound_throws() {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        UserModel u = user(2L, RolesEnum.PROJECT_ADMIN);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> projectService.assignAdmin(1L, dto));
    }

    @Test
    void assignAdmin_sameAdminAlreadyAssigned_returnsExisting() throws NotFoundException {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        UserModel u = user(2L, RolesEnum.PROJECT_ADMIN);
        EmployeeModel e = emp(10L);
        ProjectAdminAssignmentModel existing = assign(5L, p, e);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(e));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));

        ProjectDTO result = projectService.assignAdmin(1L, dto);
        assertNotNull(result);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void assignAdmin_differentAdmin_deactivatesOldAndCreatesNew() throws NotFoundException {
        ProjectModel p = proj(1L, "P1", ProjectStatusEnum.ACTIVE);
        UserModel u = user(2L, RolesEnum.PROJECT_ADMIN);
        EmployeeModel newEmp = emp(20L);
        EmployeeModel oldEmp = emp(10L);
        ProjectAdminAssignmentModel existing = assign(5L, p, oldEmp);
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO(); dto.setUserId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u));
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.of(newEmp));
        when(assignmentRepository.findByProjectIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.saveAndFlush(existing)).thenReturn(existing);
        when(assignmentRepository.save(any())).thenReturn(assign(6L, p, newEmp));

        ProjectDTO result = projectService.assignAdmin(1L, dto);
        assertNotNull(result.currentAdmin());
        assertFalse(existing.isActive());
    }
}
