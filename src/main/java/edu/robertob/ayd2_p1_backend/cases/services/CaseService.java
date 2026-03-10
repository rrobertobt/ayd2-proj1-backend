package edu.robertob.ayd2_p1_backend.cases.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStepStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CancelCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CaseFilterDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateCaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.UpdateDueDateDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseSummaryDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseStepModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseStepRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketSpecification;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeModel;
import edu.robertob.ayd2_p1_backend.casetypes.models.entities.CaseTypeStageModel;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeRepository;
import edu.robertob.ayd2_p1_backend.casetypes.repositories.CaseTypeStageRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import edu.robertob.ayd2_p1_backend.projects.models.entities.ProjectModel;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectAdminAssignmentRepository;
import edu.robertob.ayd2_p1_backend.projects.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class CaseService {

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "title",     "title",
            "status",    "status",
            "dueDate",   "dueDate",
            "createdAt", "createdAt"
    );

    private final CaseTicketRepository caseTicketRepository;
    private final CaseStepRepository caseStepRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAdminAssignmentRepository projectAdminAssignmentRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final CaseTypeStageRepository caseTypeStageRepository;
    private final EmployeeRepository employeeRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    public CaseDTO createCase(CreateCaseDTO dto) throws NotFoundException {
        ProjectModel project = findProjectById(dto.getProjectId());

        if (project.getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "No se pueden crear casos en un proyecto inactivo.");
        }

        CaseTypeModel caseType = findCaseTypeById(dto.getCaseTypeId());
        EmployeeModel createdBy = resolveCurrentEmployee();

        List<CaseTypeStageModel> stages =
                caseTypeStageRepository.findByCaseTypeIdOrderByStageOrderAsc(dto.getCaseTypeId());

        if (stages.isEmpty()) {
            throw new BadRequestException(
                    "El tipo de caso seleccionado no tiene etapas definidas. No se puede crear el caso.");
        }

        CaseTicketModel ticket = new CaseTicketModel();
        ticket.setProject(project);
        ticket.setCaseType(caseType);
        ticket.setCreatedByEmployee(createdBy);
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(CaseStatusEnum.OPEN);
        ticket.setDueDate(dto.getDueDate());

        CaseTicketModel saved = caseTicketRepository.save(ticket);

        List<CaseStepModel> steps = stages.stream().map(stage -> {
            CaseStepModel step = new CaseStepModel();
            step.setCaseTicket(saved);
            step.setCaseTypeStage(stage);
            step.setStepOrder(stage.getStageOrder());
            step.setStatus(CaseStepStatusEnum.PENDING);
            return step;
        }).toList();

        List<CaseStepModel> savedSteps = caseStepRepository.saveAll(steps);

        return toDTO(saved, savedSteps);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ - list with filters
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponseDTO<CaseSummaryDTO> getCases(CaseFilterDTO filter) {
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            try {
                CaseStatusEnum.valueOf(filter.getStatus().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Estado de caso inválido: " + filter.getStatus() +
                        ". Valores permitidos: OPEN, IN_PROGRESS, COMPLETED, CANCELED");
            }
        }

        String sortField = SORT_FIELD_MAP.getOrDefault(filter.getSortBy(), "createdAt");
        Sort sort = Sort.by(filter.direction(), sortField);
        Pageable pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(Math.max(filter.getSize(), 1), 100),
                sort
        );

        Page<CaseTicketModel> page = caseTicketRepository.findAll(
                CaseTicketSpecification.from(filter), pageable);

        var content = page.getContent().stream()
                .map(ticket -> {
                    List<CaseStepModel> steps =
                            caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(ticket.getId());
                    return toSummaryDTO(ticket, steps);
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

    // ─────────────────────────────────────────────────────────────────────────
    // READ - by project
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponseDTO<CaseSummaryDTO> getCasesByProject(Long projectId, CaseFilterDTO filter)
            throws NotFoundException {
        findProjectById(projectId);

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            try {
                CaseStatusEnum.valueOf(filter.getStatus().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Estado de caso inválido: " + filter.getStatus() +
                        ". Valores permitidos: OPEN, IN_PROGRESS, COMPLETED, CANCELED");
            }
        }

        filter.setProjectId(projectId);

        String sortField = SORT_FIELD_MAP.getOrDefault(filter.getSortBy(), "createdAt");
        Sort sort = Sort.by(filter.direction(), sortField);
        Pageable pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(Math.max(filter.getSize(), 1), 100),
                sort
        );

        Page<CaseTicketModel> page = caseTicketRepository.findAll(
                CaseTicketSpecification.from(filter), pageable);

        var content = page.getContent().stream()
                .map(ticket -> {
                    List<CaseStepModel> steps =
                            caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(ticket.getId());
                    return toSummaryDTO(ticket, steps);
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

    // ─────────────────────────────────────────────────────────────────────────
    // READ - detail
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CaseDTO getCaseById(Long caseId) throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);

        // DEVELOPER can only view cases where they have a step assigned
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDeveloper = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"));
        if (isDeveloper) {
            EmployeeModel caller = resolveCurrentEmployee();
            if (!caseStepRepository.existsByCaseTicketIdAndAssignedEmployeeId(
                    caseId, caller.getId())) {
                throw new AccessDeniedException("No tiene acceso a este caso");
            }
        }

        List<CaseStepModel> steps =
                caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(caseId);
        return toDTO(ticket, steps);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ - my assigned cases (DEVELOPER)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CaseSummaryDTO> getMyAssignedCases() {
        EmployeeModel employee = resolveCurrentEmployee();
        List<Long> caseIds = caseStepRepository
                .findDistinctCaseIdsByAssignedEmployeeId(employee.getId());
        if (caseIds.isEmpty()) return List.of();
        return caseTicketRepository.findAllById(caseIds).stream()
                .map(ticket -> {
                    List<CaseStepModel> steps =
                            caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(ticket.getId());
                    return toSummaryDTO(ticket, steps);
                })
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ - alerts (overdue and near-due cases)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CaseSummaryDTO> getCaseAlerts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EmployeeModel employee = resolveCurrentEmployee();
        LocalDate threshold = LocalDate.now().plusDays(3);

        boolean isProjectAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROJECT_ADMIN"));

        List<CaseTicketModel> alertCases;

        if (isProjectAdmin) {
            List<Long> projectIds = projectAdminAssignmentRepository
                    .findByEmployeeIdAndActiveTrue(employee.getId())
                    .stream()
                    .map(a -> a.getProject().getId())
                    .toList();

            if (projectIds.isEmpty()) return List.of();

            alertCases = caseTicketRepository.findByProjectIdIn(projectIds).stream()
                    .filter(t -> t.getStatus() != CaseStatusEnum.COMPLETED
                              && t.getStatus() != CaseStatusEnum.CANCELED)
                    .filter(t -> !t.getDueDate().isAfter(threshold))
                    .toList();
        } else {
            // DEVELOPER: cases where they have at least one step assigned
            List<Long> caseIds = caseStepRepository
                    .findDistinctCaseIdsByAssignedEmployeeId(employee.getId());

            if (caseIds.isEmpty()) return List.of();

            alertCases = caseTicketRepository.findAllById(caseIds).stream()
                    .filter(t -> t.getStatus() != CaseStatusEnum.COMPLETED
                              && t.getStatus() != CaseStatusEnum.CANCELED)
                    .filter(t -> !t.getDueDate().isAfter(threshold))
                    .toList();
        }

        return alertCases.stream()
                .map(t -> {
                    List<CaseStepModel> steps =
                            caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(t.getId());
                    return toSummaryDTO(t, steps);
                })
                .sorted((a, b) -> a.dueDate().compareTo(b.dueDate()))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE - due date
    // ─────────────────────────────────────────────────────────────────────────

    public CaseDTO updateDueDate(Long caseId, UpdateDueDateDTO dto) throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);

        if (ticket.getProject().getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "El proyecto asociado a este caso está inactivo. No se pueden realizar operaciones sobre sus casos.");
        }

        if (ticket.getStatus() == CaseStatusEnum.CANCELED ||
                ticket.getStatus() == CaseStatusEnum.COMPLETED) {
            throw new BadRequestException(
                    "No se puede modificar la fecha límite de un caso con estado: " + ticket.getStatus());
        }

        ticket.setDueDate(dto.getDueDate());
        CaseTicketModel saved = caseTicketRepository.save(ticket);
        List<CaseStepModel> steps =
                caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(caseId);
        return toDTO(saved, steps);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE - cancel
    // ─────────────────────────────────────────────────────────────────────────

    public CaseDTO cancelCase(Long caseId, CancelCaseDTO dto) throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);

        if (ticket.getProject().getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "El proyecto asociado a este caso está inactivo. No se pueden realizar operaciones sobre sus casos.");
        }

        if (ticket.getStatus() == CaseStatusEnum.CANCELED) {
            throw new BadRequestException("El caso ya está cancelado.");
        }

        if (ticket.getStatus() == CaseStatusEnum.COMPLETED) {
            throw new BadRequestException("No se puede cancelar un caso que ya está completado.");
        }

        ticket.setStatus(CaseStatusEnum.CANCELED);
        ticket.setCanceledAt(Instant.now());
        ticket.setCancelReason(dto.getReason());

        CaseTicketModel saved = caseTicketRepository.save(ticket);
        List<CaseStepModel> steps =
                caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(caseId);
        return toDTO(saved, steps);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private ProjectModel findProjectById(Long id) throws NotFoundException {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un proyecto con el ID: " + id));
    }

    private CaseTypeModel findCaseTypeById(Long id) throws NotFoundException {
        return caseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un tipo de caso con el ID: " + id));
    }

    private CaseTicketModel findCaseById(Long id) throws NotFoundException {
        return caseTicketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un caso con el ID: " + id));
    }

    private EmployeeModel resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<EmployeeModel> employee = employeeRepository.findByUserUsername(username);
        if (employee.isEmpty()) {
            throw new BadRequestException(
                    "El usuario autenticado no tiene un perfil de empleado asociado.");
        }
        return employee.get();
    }

    private int calculateProgress(List<CaseStepModel> steps) {
        if (steps.isEmpty()) return 0;
        long done = steps.stream()
                .filter(s -> s.getStatus() == CaseStepStatusEnum.APPROVED)
                .count();
        return (int) Math.round((double) done / steps.size() * 100);
    }

    private boolean isOverdue(CaseTicketModel ticket) {
        return ticket.getStatus() != CaseStatusEnum.COMPLETED &&
                ticket.getStatus() != CaseStatusEnum.CANCELED &&
                ticket.getDueDate().isBefore(LocalDate.now());
    }

    private CaseSummaryDTO toSummaryDTO(CaseTicketModel ticket, List<CaseStepModel> steps) {
        return new CaseSummaryDTO(
                ticket.getId(),
                ticket.getProject().getId(),
                ticket.getProject().getName(),
                ticket.getCaseType().getId(),
                ticket.getCaseType().getName(),
                ticket.getTitle(),
                ticket.getStatus().name(),
                ticket.getDueDate(),
                isOverdue(ticket),
                calculateProgress(steps),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private CaseDTO toDTO(CaseTicketModel ticket, List<CaseStepModel> steps) {
        List<CaseStepDTO> stepDTOs = steps.stream().map(this::toStepDTO).toList();
        EmployeeModel creator = ticket.getCreatedByEmployee();
        return new CaseDTO(
                ticket.getId(),
                ticket.getProject().getId(),
                ticket.getProject().getName(),
                ticket.getCaseType().getId(),
                ticket.getCaseType().getName(),
                creator.getId(),
                creator.getFirst_name() + " " + creator.getLast_name(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getDueDate(),
                isOverdue(ticket),
                calculateProgress(steps),
                ticket.getCanceledAt(),
                ticket.getCancelReason(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                stepDTOs
        );
    }

    private CaseStepDTO toStepDTO(CaseStepModel step) {
        String assignedName = null;
        Long assignedId = null;
        if (step.getAssignedEmployee() != null) {
            assignedId = step.getAssignedEmployee().getId();
            assignedName = step.getAssignedEmployee().getFirst_name() + " " +
                           step.getAssignedEmployee().getLast_name();
        }
        return new CaseStepDTO(
                step.getId(),
                step.getCaseTypeStage().getId(),
                step.getCaseTypeStage().getName(),
                step.getStepOrder(),
                step.getStatus().name(),
                assignedId,
                assignedName,
                step.getAssignedAt(),
                step.getStartedAt(),
                step.getSubmittedAt(),
                step.getApprovedAt(),
                step.getRejectedAt(),
                step.getRejectionReason(),
                step.getCreatedAt(),
                step.getUpdatedAt()
        );
    }
}
