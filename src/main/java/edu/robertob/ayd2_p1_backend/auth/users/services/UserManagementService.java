package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.SetPasswordDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UpdateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.UserFilterDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.OnboardingTokenModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.OnboardingTokenRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserSpecification;
import edu.robertob.ayd2_p1_backend.core.config.AppProperties;
import edu.robertob.ayd2_p1_backend.core.exceptions.DuplicateResourceException;
import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import edu.robertob.ayd2_p1_backend.core.models.entities.response.PagedResponseDTO;
import edu.robertob.ayd2_p1_backend.core.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UserManagementService {

    private static final int ONBOARDING_TOKEN_EXPIRATION_HOURS = 24;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final OnboardingTokenRepository onboardingTokenRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MailService mailService;
    private final AppProperties appProperties;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new user together with their employee profile.
     * <p>
     * If {@code createUserDTO.getPassword()} is provided, the password is set directly.
     * Otherwise an onboarding token is generated and emailed to the user so they
     * can set their own password via the public endpoint.
     *
     * @param dto data for the new user + employee
     * @return UserDTO of the created user
     */
    public UserDTO createUser(CreateUserDTO dto) throws NotFoundException {

        // 1. Validate uniqueness
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario con el nombre de usuario: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario con el correo electrónico: " + dto.getEmail());
        }

        // 2. Look up role
        RoleModel role = roleService.findRoleById(dto.getRoleId());

        // 3. Build user entity
        UserModel user = new UserModel();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(role);
        user.setActive(true);

        boolean hasPassword = StringUtils.hasText(dto.getPassword());
        if (hasPassword) {
            user.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
        } else {
            // placeholder – will be set when onboarding token is consumed
            user.setPassword_hash(passwordEncoder.encode(generateSecureToken()));
        }

        UserModel savedUser = userRepository.save(user);

        // 4. Build employee entity
        CreateUserDTO.EmployeeDataDTO empDto = dto.getEmployee();
        EmployeeModel employee = new EmployeeModel();
        employee.setFirst_name(empDto.getFirstName());
        employee.setLast_name(empDto.getLastName());
        employee.setHourly_rate(empDto.getHourlyRate());
        employee.setUser(savedUser);
        EmployeeModel savedEmployee = employeeRepository.save(employee);

        // 5. Send onboarding email if no password was provided
        if (!hasPassword) {
            sendOnboardingEmail(savedUser, empDto.getFirstName());
        }

        return userMapper.userToUserDTO(savedUser, savedEmployee);
    }

    // ── employee-side sort fields that live on the joined entity ─────────────
    private static final Set<String> EMPLOYEE_SORT_FIELDS =
            Set.of("firstName", "lastName", "hourlyRate");

    // ── user-entity field name mapping ────────────────────────────────────────
    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "username",   "username",
            "email",      "email",
            "active",     "active",
            "createdAt",  "createdAt",
            "firstName",  "first_name",
            "lastName",   "last_name",
            "hourlyRate", "hourly_rate"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // READ  –  paginated + filtered list
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a paginated, filtered list of users.
     *
     * @param filter query params (search, firstName, lastName, email, roleId, active, page, size, sortBy, sortDir)
     */
    @Transactional(readOnly = true)
    public PagedResponseDTO<UserDTO> getUsers(UserFilterDTO filter) {

        String rawSortField = SORT_FIELD_MAP.getOrDefault(filter.getSortBy(), "createdAt");
        boolean isEmployeeSort = EMPLOYEE_SORT_FIELDS.contains(filter.getSortBy());

        // For employee-side sorting we sort in-memory after fetching the page,
        // because the JPA column lives on the joined table. For user-side fields
        // we let the DB handle it.
        Sort sort = isEmployeeSort
                ? Sort.unsorted()
                : Sort.by(filter.direction(), rawSortField);

        Pageable pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(Math.max(filter.getSize(), 1), 100),
                sort
        );

        Page<UserModel> page = userRepository.findAll(UserSpecification.from(filter), pageable);

        var content = page.getContent().stream()
                .map(u -> {
                    EmployeeModel emp = employeeRepository.findByUserId(u.getId()).orElse(null);
                    return userMapper.userToUserDTO(u, emp);
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

    /**
     * Finds a single user by ID.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) throws NotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró un usuario con el ID: " + id));
        EmployeeModel emp = employeeRepository.findByUserId(id).orElse(null);
        return userMapper.userToUserDTO(user, emp);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Partially updates a user and/or their employee profile.
     */
    public UserDTO updateUser(Long id, UpdateUserDTO dto) throws NotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró un usuario con el ID: " + id));

        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new DuplicateResourceException(
                        "Ya existe un usuario con el nombre de usuario: " + dto.getUsername());
            }
            user.setUsername(dto.getUsername());
        }

        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException(
                        "Ya existe un usuario con el correo electrónico: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getRoleId() != null) {
            RoleModel role = roleService.findRoleById(dto.getRoleId());
            user.setRole(role);
        }

        UserModel savedUser = userRepository.save(user);

        EmployeeModel emp = employeeRepository.findByUserId(id).orElse(null);

        if (dto.getEmployee() != null && emp != null) {
            UpdateUserDTO.EmployeeUpdateDataDTO empDto = dto.getEmployee();
            if (StringUtils.hasText(empDto.getFirstName())) emp.setFirst_name(empDto.getFirstName());
            if (StringUtils.hasText(empDto.getLastName())) emp.setLast_name(empDto.getLastName());
            if (empDto.getHourlyRate() != null) emp.setHourly_rate(empDto.getHourlyRate());
            emp = employeeRepository.save(emp);
        }

        return userMapper.userToUserDTO(savedUser, emp);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOGGLE STATUS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Activates or deactivates a user account.
     */
    public UserDTO toggleUserStatus(Long id) throws NotFoundException {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró un usuario con el ID: " + id));
        user.setActive(!user.isActive());
        UserModel saved = userRepository.save(user);
        EmployeeModel emp = employeeRepository.findByUserId(id).orElse(null);
        return userMapper.userToUserDTO(saved, emp);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ONBOARDING – SET PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates the onboarding token and sets the user's password.
     * After success, marks the token as used and sends a confirmation email.
     */
    public void setPasswordFromOnboarding(SetPasswordDTO dto) throws InvalidTokenException {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidTokenException("Las contraseñas no coinciden.");
        }

        OnboardingTokenModel tokenModel = onboardingTokenRepository
                .findByTokenAndUsedFalse(dto.getToken())
                .orElseThrow(() -> new InvalidTokenException(
                        "El token de onboarding es inválido o ya fue utilizado."));

        if (tokenModel.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("El token de onboarding ha expirado.");
        }

        // Set the new password
        UserModel user = tokenModel.getUser();
        user.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
        user.setActive(true);
        userRepository.save(user);

        // Mark token as used
        tokenModel.setUsed(true);
        onboardingTokenRepository.save(tokenModel);

        // Send confirmation email
        EmployeeModel emp = employeeRepository.findByUserId(user.getId()).orElse(null);
        String firstName = emp != null ? emp.getFirst_name() : user.getUsername();

        mailService.sendHtmlEmail(
                user.getEmail(),
                "Tu contraseña ha sido establecida",
                "email/password-set-confirmation",
                Map.of(
                        "firstName", firstName,
                        "username", user.getUsername()
                )
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void sendOnboardingEmail(UserModel user, String firstName) {
        String rawToken = generateSecureToken();

        OnboardingTokenModel tokenModel = new OnboardingTokenModel();
        tokenModel.setUser(user);
        tokenModel.setToken(rawToken);
        tokenModel.setUsed(false);
        tokenModel.setExpiresAt(Instant.now().plus(ONBOARDING_TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS));
        onboardingTokenRepository.save(tokenModel);

        // String link = appProperties.getBackendHost()
        //         + "/api/v1/users/onboarding?token=" + rawToken;
        String link = appProperties.getFrontendHost()
                + "/onboarding?token=" + rawToken;

        mailService.sendHtmlEmail(
                user.getEmail(),
                "Bienvenido – Establece tu contraseña",
                "email/onboarding-invitation",
                Map.of(
                        "firstName", firstName,
                        "username", user.getUsername(),
                        "onboardingLink", link,
                        "expirationHours", ONBOARDING_TOKEN_EXPIRATION_HOURS
                )
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

