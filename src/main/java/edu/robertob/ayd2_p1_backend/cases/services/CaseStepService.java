package edu.robertob.ayd2_p1_backend.cases.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.enums.CaseStepStatusEnum;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.ApproveStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.AssignStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.CreateWorklogDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.request.RejectStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.CaseStepDTO;
import edu.robertob.ayd2_p1_backend.cases.models.dto.response.WorklogDTO;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseStepModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;
import edu.robertob.ayd2_p1_backend.cases.models.entities.WorkLogModel;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseStepRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.CaseTicketRepository;
import edu.robertob.ayd2_p1_backend.cases.repositories.WorkLogRepository;
import edu.robertob.ayd2_p1_backend.core.exceptions.BadRequestException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.projects.enums.ProjectStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class CaseStepService {

    private final CaseTicketRepository caseTicketRepository;
    private final CaseStepRepository caseStepRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkLogRepository workLogRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // READ - list steps of a case
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CaseStepDTO> getSteps(Long caseId) throws NotFoundException {
        findCaseById(caseId);
        List<CaseStepModel> steps = caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(caseId);
        return steps.stream().map(this::toStepDTO).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASSIGN developer to a step
    // ─────────────────────────────────────────────────────────────────────────

    public CaseStepDTO assignStep(Long caseId, Long stepId, AssignStepDTO dto) throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);
        CaseStepModel step = findStepByCaseAndId(caseId, stepId);

        if (ticket.getProject().getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "El proyecto asociado a este caso está inactivo. No se pueden realizar operaciones sobre sus casos.");
        }

        if (ticket.getStatus() == CaseStatusEnum.CANCELED ||
                ticket.getStatus() == CaseStatusEnum.COMPLETED) {
            throw new BadRequestException(
                    "No se puede asignar un paso a un caso con estado: " + ticket.getStatus());
        }

        if (step.getStatus() == CaseStepStatusEnum.APPROVED) {
            throw new BadRequestException("El paso ya fue aprobado y no puede ser reasignado.");
        }

        // Validate that all previous steps in the workflow have been APPROVED first
        List<CaseStepModel> allSteps =
                caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(caseId);
        boolean hasPendingPreviousStep = allSteps.stream()
                .filter(s -> s.getStepOrder() < step.getStepOrder())
                .anyMatch(s -> s.getStatus() != CaseStepStatusEnum.APPROVED);
        if (hasPendingPreviousStep) {
            throw new BadRequestException(
                    "No se puede asignar este paso porque hay pasos anteriores que aún no han sido aprobados.");
        }

        EmployeeModel employee = findEmployeeByUserId(dto.getUserId());

        step.setAssignedEmployee(employee);
        step.setStatus(CaseStepStatusEnum.ASSIGNED);
        step.setAssignedAt(Instant.now());

        if (ticket.getStatus() == CaseStatusEnum.OPEN) {
            ticket.setStatus(CaseStatusEnum.IN_PROGRESS);
            caseTicketRepository.save(ticket);
        }

        CaseStepModel saved = caseStepRepository.save(step);
        return toStepDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APPROVE step and optionally assign next
    // ─────────────────────────────────────────────────────────────────────────

    public CaseStepDTO approveStep(Long caseId, Long stepId, ApproveStepDTO dto) throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);
        CaseStepModel step = findStepByCaseAndId(caseId, stepId);

        if (ticket.getProject().getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "El proyecto asociado a este caso está inactivo. No se pueden realizar operaciones sobre sus casos.");
        }

        if (!workLogRepository.existsByCaseStepId(stepId)) {
            throw new BadRequestException(
                    "No se puede aprobar un paso sin que el desarrollador haya registrado al menos un worklog.");
        }

        if (step.getStatus() != CaseStepStatusEnum.SUBMITTED &&
                step.getStatus() != CaseStepStatusEnum.IN_PROGRESS &&
                step.getStatus() != CaseStepStatusEnum.ASSIGNED) {
            throw new BadRequestException(
                    "Solo se pueden aprobar pasos en estado ASSIGNED, IN_PROGRESS o SUBMITTED. " +
                    "Estado actual: " + step.getStatus());
        }

        step.setStatus(CaseStepStatusEnum.APPROVED);
        step.setApprovedAt(Instant.now());
        caseStepRepository.save(step);

        // Find next step
        List<CaseStepModel> allSteps =
                caseStepRepository.findByCaseTicketIdOrderByStepOrderAsc(caseId);

        Optional<CaseStepModel> nextStep = allSteps.stream()
                .filter(s -> s.getStepOrder() == step.getStepOrder() + 1)
                .findFirst();

        if (nextStep.isPresent()) {
            // There is a next step – assign it if nextAssigneeUserId provided
            if (dto.getNextAssigneeUserId() != null) {
                EmployeeModel nextEmployee = findEmployeeByUserId(dto.getNextAssigneeUserId());

                CaseStepModel next = nextStep.get();
                next.setAssignedEmployee(nextEmployee);
                next.setStatus(CaseStepStatusEnum.ASSIGNED);
                next.setAssignedAt(Instant.now());
                caseStepRepository.save(next);
            }
        } else {
            // No more steps – complete the case
            ticket.setStatus(CaseStatusEnum.COMPLETED);
            caseTicketRepository.save(ticket);
        }

        return toStepDTO(caseStepRepository.findById(stepId).orElseThrow());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REJECT step
    // ─────────────────────────────────────────────────────────────────────────

    public CaseStepDTO rejectStep(Long caseId, Long stepId, RejectStepDTO dto) throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);
        CaseStepModel step = findStepByCaseAndId(caseId, stepId);

        if (ticket.getProject().getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "El proyecto asociado a este caso está inactivo. No se pueden realizar operaciones sobre sus casos.");
        }

        if (!workLogRepository.existsByCaseStepId(stepId)) {
            throw new BadRequestException(
                    "No se puede rechazar un paso sin que el desarrollador haya registrado al menos un worklog.");
        }

        if (step.getStatus() != CaseStepStatusEnum.SUBMITTED &&
                step.getStatus() != CaseStepStatusEnum.IN_PROGRESS &&
                step.getStatus() != CaseStepStatusEnum.ASSIGNED) {
            throw new BadRequestException(
                    "Solo se pueden rechazar pasos en estado ASSIGNED, IN_PROGRESS o SUBMITTED. " +
                    "Estado actual: " + step.getStatus());
        }

        step.setStatus(CaseStepStatusEnum.IN_PROGRESS);
        step.setRejectedAt(Instant.now());
        step.setRejectionReason(dto.getReason());

        CaseStepModel saved = caseStepRepository.save(step);
        return toStepDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private CaseTicketModel findCaseById(Long caseId) throws NotFoundException {
        return caseTicketRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un caso con el ID: " + caseId));
    }

    private CaseStepModel findStepByCaseAndId(Long caseId, Long stepId) throws NotFoundException {
        return caseStepRepository.findByIdAndCaseTicketId(stepId, caseId)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró el paso con ID " + stepId +
                        " para el caso con ID " + caseId));
    }

    private EmployeeModel findEmployeeByUserId(Long userId) throws NotFoundException {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un empleado para el usuario con ID: " + userId));
    }

    private EmployeeModel resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return employeeRepository.findByUserUsername(auth.getName())
                .orElseThrow(() -> new BadRequestException(
                        "El usuario autenticado no tiene un perfil de empleado asociado."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WORKLOGS
    // ─────────────────────────────────────────────────────────────────────────

    public WorklogDTO createWorklog(Long caseId, Long stepId, CreateWorklogDTO dto)
            throws NotFoundException {
        CaseTicketModel ticket = findCaseById(caseId);

        if (ticket.getProject().getStatus() == ProjectStatusEnum.INACTIVE) {
            throw new BadRequestException(
                    "El proyecto asociado a este caso está inactivo. No se pueden realizar operaciones sobre sus casos.");
        }

        if (ticket.getStatus() == CaseStatusEnum.CANCELED ||
                ticket.getStatus() == CaseStatusEnum.COMPLETED) {
            throw new BadRequestException(
                    "No se puede registrar trabajo en un caso con estado: " + ticket.getStatus());
        }

        CaseStepModel step = findStepByCaseAndId(caseId, stepId);

        if (step.getStatus() != CaseStepStatusEnum.ASSIGNED &&
                step.getStatus() != CaseStepStatusEnum.IN_PROGRESS) {
            throw new BadRequestException(
                    "Solo se puede registrar trabajo en pasos con estado ASSIGNED o IN_PROGRESS. " +
                    "Estado actual: " + step.getStatus());
        }

        EmployeeModel caller = resolveCurrentEmployee();

        if (step.getAssignedEmployee() == null ||
                !step.getAssignedEmployee().getId().equals(caller.getId())) {
            throw new AccessDeniedException(
                    "Solo el desarrollador asignado al paso puede registrar trabajo");
        }

        // Record when work actually started (only on first worklog)
        if (step.getStatus() == CaseStepStatusEnum.ASSIGNED) {
            step.setStartedAt(Instant.now());
        }

        // Developer is submitting their work for review
        step.setStatus(CaseStepStatusEnum.SUBMITTED);
        step.setSubmittedAt(Instant.now());
        caseStepRepository.save(step);

        WorkLogModel worklog = new WorkLogModel();
        worklog.setCaseStep(step);
        worklog.setEmployee(caller);
        worklog.setComment(dto.getComment());
        worklog.setHoursSpent(dto.getHoursSpent());

        WorkLogModel saved = workLogRepository.save(worklog);
        return toWorklogDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET worklogs of a step
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WorklogDTO> getWorklogs(Long caseId, Long stepId) throws NotFoundException {
        findCaseById(caseId);
        findStepByCaseAndId(caseId, stepId);
        return workLogRepository.findByCaseStepIdOrderByCreatedAtAsc(stepId)
                .stream().map(this::toWorklogDTO).toList();
    }

    private WorklogDTO toWorklogDTO(WorkLogModel worklog) {
        EmployeeModel emp = worklog.getEmployee();
        return new WorklogDTO(
                worklog.getId(),
                worklog.getCaseStep().getId(),
                emp.getId(),
                emp.getFirst_name() + " " + emp.getLast_name(),
                worklog.getComment(),
                worklog.getHoursSpent(),
                worklog.getCreatedAt(),
                worklog.getUpdatedAt()
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
