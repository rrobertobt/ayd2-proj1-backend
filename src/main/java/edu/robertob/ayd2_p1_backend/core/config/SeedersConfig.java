package edu.robertob.ayd2_p1_backend.core.config;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.services.EmployeeService;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedersConfig implements CommandLineRunner {

    // Hash from db/inserts.sql (password already bcrypt-encoded)
    private static final String SUPERADMIN_PASSWORD_HASH =
            "$2a$12$QhpH.DIM3W1FuL/7JC/o0OO9YM5N.BDRn.KpKjD8MRPzftoBDyTZu";

    private final RoleService roleService;
    private final UserService userService;
    private final EmployeeService employeeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {
        log.info("Starting database seeding...");
        seedRoles();

        RoleModel systemAdminRole = roleService.findRoleByCodeOrNull(RolesEnum.SYSTEM_ADMIN);
        if (systemAdminRole == null) {
            throw new IllegalStateException("SYSTEM_ADMIN role could not be created during seeding.");
        }

        UserModel superAdmin = userService.createUserIfNotExists(
                "superadmin",
                "superadmin@tracker.com",
                SUPERADMIN_PASSWORD_HASH,
                true,
                systemAdminRole
        );

        employeeService.createEmployeeIfNotExists(superAdmin, "Super", "Admin", 0.00d);
        log.info("Database seed completed successfully.");
    }

    private void seedRoles() {
        roleService.createRoleIfNotExists(
                RolesEnum.SYSTEM_ADMIN,
                "System Administrator",
                "Full access to all system features and settings."
        );

        roleService.createRoleIfNotExists(
                RolesEnum.PROJECT_ADMIN,
                "Project Administrator",
                "Can manage projects and assign tasks."
        );

        roleService.createRoleIfNotExists(
                RolesEnum.DEVELOPER,
                "Developer",
                "Can view and update assigned tasks."
        );
    }
}
