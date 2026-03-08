package edu.robertob.ayd2_p1_backend.projects.services;

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
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ProjectService {

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "name",      "name",
            "status",    "status",
            "createdAt", "createdAt"
    );

    private final ProjectRepository projectRepository;
    private final ProjectAdminAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    public ProjectDTO createProject(CreateProjectDTO dto) {
        ProjectModel project = new ProjectModel();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStatus(ProjectStatusEnum.ACTIVE);
        ProjectModel saved = projectRepository.save(project);
        return toDTO(saved, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponseDTO<ProjectDTO> getProjects(ProjectFilterDTO filter) {
        if (StringUtils.hasText(filter.getStatus())) {
            try {
                ProjectStatusEnum.valueOf(filter.getStatus().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Estado de proyecto inválido: " + filter.getStatus() +
                        ". Valores permitidos: ACTIVE, INACTIVE");
            }
        }

        String sortField = SORT_FIELD_MAP.getOrDefault(filter.getSortBy(), "createdAt");
        Sort sort = Sort.by(filter.direction(), sortField);
        Pageable pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(Math.max(filter.getSize(), 1), 100),
                sort
        );

        Page<ProjectModel> page = projectRepository.findAll(
                ProjectSpecification.from(filter), pageable);

        var content = page.getContent().stream()
                .map(p -> {
                    Optional<ProjectAdminAssignmentModel> assignment =
                            assignmentRepository.findByProjectIdAndActiveTrue(p.getId());
                    return toDTO(p, assignment.orElse(null));
                })
                .toList();

        return new PagedResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) throws NotFoundException {
        ProjectModel project = findProjectById(id);
        Optional<ProjectAdminAssignmentModel> assignment =
                assignmentRepository.findByProjectIdAndActiveTrue(id);
        return toDTO(project, assignment.orElse(null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public ProjectDTO updateProject(Long id, UpdateProjectDTO dto) throws NotFoundException {
        ProjectModel project = findProjectById(id);
        if (StringUtils.hasText(dto.getName())) {
            project.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        ProjectModel saved = projectRepository.save(project);
        Optional<ProjectAdminAssignmentModel> assignment =
                assignmentRepository.findByProjectIdAndActiveTrue(id);
        return toDTO(saved, assignment.orElse(null));
    }

    public ProjectDTO toggleStatus(Long id) throws NotFoundException {
        ProjectModel project = findProjectById(id);
        if (project.getStatus() == ProjectStatusEnum.ACTIVE) {
            project.setStatus(ProjectStatusEnum.INACTIVE);
        } else {
            project.setStatus(ProjectStatusEnum.ACTIVE);
        }
        ProjectModel saved = projectRepository.save(project);
        Optional<ProjectAdminAssignmentModel> assignment =
                assignmentRepository.findByProjectIdAndActiveTrue(id);
        return toDTO(saved, assignment.orElse(null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASSIGN ADMIN
    // ─────────────────────────────────────────────────────────────────────────

    public ProjectDTO assignAdmin(Long projectId, AssignProjectAdminDTO dto) throws NotFoundException {
        ProjectModel project = findProjectById(projectId);

        UserModel user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un usuario con el ID: " + dto.getUserId()));

        if (user.getRole().getCode() != RolesEnum.PROJECT_ADMIN) {
            throw new BadRequestException(
                    "El usuario con ID " + dto.getUserId() + " no tiene el rol PROJECT_ADMIN. " +
                    "Rol actual: " + user.getRole().getCode().getCode());
        }

        EmployeeModel employee = employeeRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un perfil de empleado para el usuario con ID: " + dto.getUserId()));

        // Deactivate current active assignment if one exists
        assignmentRepository.findByProjectIdAndActiveTrue(projectId)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    existing.setEndDate(LocalDate.now());
                    assignmentRepository.save(existing);
                });

        // Create the new assignment
        ProjectAdminAssignmentModel newAssignment = new ProjectAdminAssignmentModel();
        newAssignment.setProject(project);
        newAssignment.setEmployee(employee);
        newAssignment.setStartDate(LocalDate.now());
        newAssignment.setActive(true);
        ProjectAdminAssignmentModel savedAssignment = assignmentRepository.save(newAssignment);

        return toDTO(project, savedAssignment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private ProjectModel findProjectById(Long id) throws NotFoundException {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un proyecto con el ID: " + id));
    }

    private ProjectDTO toDTO(ProjectModel project, ProjectAdminAssignmentModel assignment) {
        ProjectDTO.AdminInfoDTO adminInfo = null;
        if (assignment != null) {
            EmployeeModel emp = assignment.getEmployee();
            adminInfo = new ProjectDTO.AdminInfoDTO(
                    assignment.getId(),
                    emp.getId(),
                    emp.getFirst_name(),
                    emp.getLast_name()
            );
        }
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                adminInfo
        );
    }
}
