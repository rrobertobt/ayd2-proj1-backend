package edu.robertob.ayd2_p1_backend.projects.controllers;

import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.AssignProjectAdminDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.CreateProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.ProjectFilterDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.request.UpdateProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.models.dto.response.ProjectDTO;
import edu.robertob.ayd2_p1_backend.projects.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    // ── createProject ─────────────────────────────────────────────────────────

    @Test
    void createProject_delegatesToServiceAndReturnsDTO() {
        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("Sistema ERP");
        ProjectDTO expected = buildProjectDTO(1L, "Sistema ERP", "ACTIVE");
        when(projectService.createProject(dto)).thenReturn(expected);

        ProjectDTO result = projectController.createProject(dto);

        assertSame(expected, result);
        verify(projectService).createProject(dto);
    }

    // ── getProjects ───────────────────────────────────────────────────────────

    @Test
    void getProjects_noFilter_delegatesToService() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        PagedResponseDTO<ProjectDTO> expected = new PagedResponseDTO<>(List.of(), 0, 10, 0L, 0, true);
        when(projectService.getProjects(filter)).thenReturn(expected);

        PagedResponseDTO<ProjectDTO> result = projectController.getProjects(filter);

        assertSame(expected, result);
        verify(projectService).getProjects(filter);
    }

    @Test
    void getProjects_withStatusFilter_delegatesToService() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("ACTIVE");
        PagedResponseDTO<ProjectDTO> expected = new PagedResponseDTO<>(
                List.of(buildProjectDTO(1L, "P1", "ACTIVE")), 0, 10, 1L, 1, true);
        when(projectService.getProjects(filter)).thenReturn(expected);

        PagedResponseDTO<ProjectDTO> result = projectController.getProjects(filter);

        assertSame(expected, result);
    }

    // ── getProjectById ────────────────────────────────────────────────────────

    @Test
    void getProjectById_delegatesToService() throws NotFoundException {
        ProjectDTO expected = buildProjectDTO(1L, "P1", "ACTIVE");
        when(projectService.getProjectById(1L)).thenReturn(expected);

        ProjectDTO result = projectController.getProjectById(1L);

        assertSame(expected, result);
        verify(projectService).getProjectById(1L);
    }

    @Test
    void getProjectById_propagatesNotFoundException() throws NotFoundException {
        when(projectService.getProjectById(99L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> projectController.getProjectById(99L));
    }

    // ── updateProject ─────────────────────────────────────────────────────────

    @Test
    void updateProject_delegatesToService() throws NotFoundException {
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("Updated");
        ProjectDTO expected = buildProjectDTO(1L, "Updated", "ACTIVE");
        when(projectService.updateProject(1L, dto)).thenReturn(expected);

        ProjectDTO result = projectController.updateProject(1L, dto);

        assertSame(expected, result);
        verify(projectService).updateProject(1L, dto);
    }

    @Test
    void updateProject_propagatesNotFoundException() throws NotFoundException {
        UpdateProjectDTO dto = new UpdateProjectDTO();
        when(projectService.updateProject(eq(99L), any())).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> projectController.updateProject(99L, dto));
    }

    // ── toggleStatus ──────────────────────────────────────────────────────────

    @Test
    void toggleStatus_delegatesToService() throws NotFoundException {
        ProjectDTO expected = buildProjectDTO(1L, "P1", "INACTIVE");
        when(projectService.toggleStatus(1L)).thenReturn(expected);

        ProjectDTO result = projectController.toggleStatus(1L);

        assertSame(expected, result);
        verify(projectService).toggleStatus(1L);
    }

    @Test
    void toggleStatus_propagatesNotFoundException() throws NotFoundException {
        when(projectService.toggleStatus(99L)).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> projectController.toggleStatus(99L));
    }

    // ── assignAdmin ───────────────────────────────────────────────────────────

    @Test
    void assignAdmin_delegatesToService() throws NotFoundException {
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(20L);
        ProjectDTO expected = buildProjectDTO(1L, "P1", "ACTIVE");
        when(projectService.assignAdmin(1L, dto)).thenReturn(expected);

        ProjectDTO result = projectController.assignAdmin(1L, dto);

        assertSame(expected, result);
        verify(projectService).assignAdmin(1L, dto);
    }

    @Test
    void assignAdmin_propagatesNotFoundException() throws NotFoundException {
        AssignProjectAdminDTO dto = new AssignProjectAdminDTO();
        dto.setUserId(99L);
        when(projectService.assignAdmin(eq(1L), any())).thenThrow(new NotFoundException("Not found"));

        assertThrows(NotFoundException.class, () -> projectController.assignAdmin(1L, dto));
    }

    // ── builder helper ────────────────────────────────────────────────────────

    private static ProjectDTO buildProjectDTO(Long id, String name, String status) {
        return new ProjectDTO(id, name, "description", status, Instant.now(), Instant.now(), null);
    }
}
