package edu.robertob.ayd2_p1_backend.reports.repositories;

import edu.robertob.ayd2_p1_backend.reports.models.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.robertob.ayd2_p1_backend.cases.models.entities.CaseTicketModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<CaseTicketModel, Long> {

    // ── 1. Project case count (optionally filtered by project status) ──────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.ProjectCaseCountDTO(
                p.id, p.name, CAST(p.status AS string), COUNT(ct.id)
            )
            FROM ProjectModel p
            LEFT JOIN CaseTicketModel ct ON ct.project.id = p.id
            WHERE (:status IS NULL OR CAST(p.status AS string) = :status)
            GROUP BY p.id, p.name, p.status
            ORDER BY COUNT(ct.id) DESC
            """)
    List<ProjectCaseCountDTO> projectCaseCount(@Param("status") String status);

    // ── 2. Hours & money by project ────────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.HoursAndMoneyDTO(
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM CaseTicketModel ct
            JOIN CaseStepModel cs ON cs.caseTicket.id = ct.id
            JOIN WorkLogModel wl ON wl.caseStep.id = cs.id
            JOIN EmployeeModel e ON e.id = wl.employee.id
            WHERE ct.project.id = :projectId
            """)
    HoursAndMoneyDTO hoursAndMoneyByProject(@Param("projectId") Long projectId);

    // ── 3. Hours & money by developer ─────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.HoursAndMoneyDTO(
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM WorkLogModel wl
            JOIN EmployeeModel e ON e.id = wl.employee.id
            JOIN CaseStepModel cs ON cs.id = wl.caseStep.id
            JOIN CaseTicketModel ct ON ct.id = cs.caseTicket.id
            WHERE wl.employee.id = :employeeId
            """)
    HoursAndMoneyDTO hoursAndMoneyByDeveloper(@Param("employeeId") Long employeeId);

    // ── 4. Hours & money by case type ─────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.HoursAndMoneyDTO(
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM CaseTicketModel ct
            JOIN CaseStepModel cs ON cs.caseTicket.id = ct.id
            JOIN WorkLogModel wl ON wl.caseStep.id = cs.id
            JOIN EmployeeModel e ON e.id = wl.employee.id
            WHERE ct.caseType.id = :caseTypeId
            """)
    HoursAndMoneyDTO hoursAndMoneyByCaseType(@Param("caseTypeId") Long caseTypeId);

    // ── 5. Hours & money by date range ────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.HoursAndMoneyDTO(
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM WorkLogModel wl
            JOIN EmployeeModel e ON e.id = wl.employee.id
            JOIN CaseStepModel cs ON cs.id = wl.caseStep.id
            JOIN CaseTicketModel ct ON ct.id = cs.caseTicket.id
            WHERE wl.createdAt >= :from AND wl.createdAt <= :to
            """)
    HoursAndMoneyDTO hoursAndMoneyByDateRange(@Param("from") Instant from, @Param("to") Instant to);

    // ── 6. Developer report ────────────────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.DeveloperReportDTO(
                e.id,
                e.first_name,
                e.last_name,
                u.username,
                e.hourly_rate,
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM EmployeeModel e
            JOIN UserModel u ON u.id = e.user.id
            LEFT JOIN WorkLogModel wl ON wl.employee.id = e.id
            LEFT JOIN CaseStepModel cs ON cs.id = wl.caseStep.id
            LEFT JOIN CaseTicketModel ct ON ct.id = cs.caseTicket.id
            WHERE (CAST(:search AS string) IS NULL
                   OR LOWER(e.first_name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(e.last_name)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(u.username)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            GROUP BY e.id, e.first_name, e.last_name, u.username, e.hourly_rate
            ORDER BY e.last_name ASC, e.first_name ASC
            """)
    List<DeveloperReportDTO> developerReport(@Param("search") String search);

    // ── 7. Project report ─────────────────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.ProjectReportDTO(
                p.id,
                p.name,
                CAST(p.status AS string),
                COUNT(ct.id),
                SUM(CASE WHEN ct.status = edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.OPEN THEN 1 ELSE 0 END),
                SUM(CASE WHEN ct.status = edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.IN_PROGRESS THEN 1 ELSE 0 END),
                SUM(CASE WHEN ct.status = edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.COMPLETED THEN 1 ELSE 0 END),
                SUM(CASE WHEN ct.status = edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.CANCELED THEN 1 ELSE 0 END),
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM ProjectModel p
            LEFT JOIN CaseTicketModel ct ON ct.project.id = p.id
            LEFT JOIN CaseStepModel cs ON cs.caseTicket.id = ct.id
            LEFT JOIN WorkLogModel wl ON wl.caseStep.id = cs.id
            LEFT JOIN EmployeeModel e ON e.id = wl.employee.id
            WHERE (:status IS NULL OR CAST(p.status AS string) = :status)
              AND (CAST(:search AS string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            GROUP BY p.id, p.name, p.status
            ORDER BY p.name ASC
            """)
    List<ProjectReportDTO> projectReport(@Param("status") String status, @Param("search") String search);

    // ── 8. Developer with most cases participated ──────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.TopDeveloperDTO(
                e.id, e.first_name, e.last_name, u.username,
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM EmployeeModel e
            JOIN UserModel u ON u.id = e.user.id
            JOIN CaseStepModel cs ON cs.assignedEmployee.id = e.id
            JOIN CaseTicketModel ct ON ct.id = cs.caseTicket.id
            LEFT JOIN WorkLogModel wl ON wl.employee.id = e.id AND wl.caseStep.id = cs.id
            GROUP BY e.id, e.first_name, e.last_name, u.username
            ORDER BY COUNT(DISTINCT ct.id) DESC
            LIMIT 1
            """)
    Optional<TopDeveloperDTO> developerWithMostCases();

    // ── 9. Developer paid the most ─────────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.TopDeveloperDTO(
                e.id, e.first_name, e.last_name, u.username,
                COUNT(DISTINCT ct.id),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM EmployeeModel e
            JOIN UserModel u ON u.id = e.user.id
            JOIN WorkLogModel wl ON wl.employee.id = e.id
            JOIN CaseStepModel cs ON cs.id = wl.caseStep.id
            JOIN CaseTicketModel ct ON ct.id = cs.caseTicket.id
            GROUP BY e.id, e.first_name, e.last_name, u.username
            ORDER BY SUM(wl.hoursSpent * e.hourly_rate) DESC
            LIMIT 1
            """)
    Optional<TopDeveloperDTO> developerPaidTheMost();

    // ── 10. Project with most completed cases ─────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.TopProjectDTO(
                p.id, p.name, COUNT(ct.id)
            )
            FROM ProjectModel p
            JOIN CaseTicketModel ct ON ct.project.id = p.id
            WHERE ct.status = edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.COMPLETED
            GROUP BY p.id, p.name
            ORDER BY COUNT(ct.id) DESC
            LIMIT 1
            """)
    Optional<TopProjectDTO> projectWithMostCompletedCases();

    // ── 11. Project with most canceled cases ──────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.TopProjectDTO(
                p.id, p.name, COUNT(ct.id)
            )
            FROM ProjectModel p
            JOIN CaseTicketModel ct ON ct.project.id = p.id
            WHERE ct.status = edu.robertob.ayd2_p1_backend.cases.enums.CaseStatusEnum.CANCELED
            GROUP BY p.id, p.name
            ORDER BY COUNT(ct.id) DESC
            LIMIT 1
            """)
    Optional<TopProjectDTO> projectWithMostCanceledCases();

    // ── 12. Cases by project ──────────────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.CaseReportDTO(
                ct.id,
                ct.title,
                CAST(ct.status AS string),
                p.name,
                ctype.name,
                CONCAT(creator.first_name, ' ', creator.last_name),
                ct.dueDate,
                ct.createdAt,
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM CaseTicketModel ct
            JOIN ProjectModel p ON p.id = ct.project.id
            JOIN CaseTypeModel ctype ON ctype.id = ct.caseType.id
            JOIN EmployeeModel creator ON creator.id = ct.createdByEmployee.id
            LEFT JOIN CaseStepModel cs ON cs.caseTicket.id = ct.id
            LEFT JOIN WorkLogModel wl ON wl.caseStep.id = cs.id
            LEFT JOIN EmployeeModel e ON e.id = wl.employee.id
            WHERE ct.project.id = :projectId
            GROUP BY ct.id, ct.title, ct.status, p.name, ctype.name,
                     creator.first_name, creator.last_name, ct.dueDate, ct.createdAt
            ORDER BY ct.createdAt DESC
            """)
    List<CaseReportDTO> casesByProject(@Param("projectId") Long projectId);

    // ── 13. Cases by developer (participated in) ──────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.CaseReportDTO(
                ct.id,
                ct.title,
                CAST(ct.status AS string),
                p.name,
                ctype.name,
                CONCAT(creator.first_name, ' ', creator.last_name),
                ct.dueDate,
                ct.createdAt,
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * emp.hourly_rate), 0.0)
            )
            FROM CaseTicketModel ct
            JOIN ProjectModel p ON p.id = ct.project.id
            JOIN CaseTypeModel ctype ON ctype.id = ct.caseType.id
            JOIN EmployeeModel creator ON creator.id = ct.createdByEmployee.id
            JOIN CaseStepModel cs ON cs.caseTicket.id = ct.id AND cs.assignedEmployee.id = :employeeId
            LEFT JOIN WorkLogModel wl ON wl.caseStep.id = cs.id AND wl.employee.id = :employeeId
            LEFT JOIN EmployeeModel emp ON emp.id = wl.employee.id
            GROUP BY ct.id, ct.title, ct.status, p.name, ctype.name,
                     creator.first_name, creator.last_name, ct.dueDate, ct.createdAt
            ORDER BY ct.createdAt DESC
            """)
    List<CaseReportDTO> casesByDeveloper(@Param("employeeId") Long employeeId);

    // ── 14. Cases by case type ────────────────────────────────────────────────
    @Query("""
            SELECT new edu.robertob.ayd2_p1_backend.reports.models.dto.CaseReportDTO(
                ct.id,
                ct.title,
                CAST(ct.status AS string),
                p.name,
                ctype.name,
                CONCAT(creator.first_name, ' ', creator.last_name),
                ct.dueDate,
                ct.createdAt,
                COALESCE(SUM(wl.hoursSpent), 0.0),
                COALESCE(SUM(wl.hoursSpent * e.hourly_rate), 0.0)
            )
            FROM CaseTicketModel ct
            JOIN ProjectModel p ON p.id = ct.project.id
            JOIN CaseTypeModel ctype ON ctype.id = ct.caseType.id
            JOIN EmployeeModel creator ON creator.id = ct.createdByEmployee.id
            LEFT JOIN CaseStepModel cs ON cs.caseTicket.id = ct.id
            LEFT JOIN WorkLogModel wl ON wl.caseStep.id = cs.id
            LEFT JOIN EmployeeModel e ON e.id = wl.employee.id
            WHERE ct.caseType.id = :caseTypeId
            GROUP BY ct.id, ct.title, ct.status, p.name, ctype.name,
                     creator.first_name, creator.last_name, ct.dueDate, ct.createdAt
            ORDER BY ct.createdAt DESC
            """)
    List<CaseReportDTO> casesByCaseType(@Param("caseTypeId") Long caseTypeId);
}
