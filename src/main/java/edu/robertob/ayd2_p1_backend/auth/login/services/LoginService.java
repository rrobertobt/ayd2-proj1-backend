package edu.robertob.ayd2_p1_backend.auth.login.services;

import edu.robertob.ayd2_p1_backend.auth.jwt.services.JwtGeneratorService;
import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.services.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.robertob.ayd2_p1_backend.auth.login.models.dto.request.LoginDTO;
import edu.robertob.ayd2_p1_backend.auth.login.models.dto.response.LoginResponseDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class LoginService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtGeneratorService jwtGeneratorService;
    private final EmployeeRepository employeeRepository;
    private final UserMapper userMapper;

    /**
     * Autentica a un usuario en el sistema utilizando su correo o nombre de usuario
     * y contraseña.
     * Si las credenciales son válidas, genera y retorna un token JWT junto con
     * información del usuario.
     *
     * @param loginDTO objeto que contiene el nombre de usuario y la contraseña
     * @return LoginResponseDTO con el nombre de usuario, el rol y el token JWT
     * @throws NotFoundException       si el usuario no se encuentra en el sistema
     * @throws BadCredentialsException si la contraseña es incorrecta o el usuario
     *                                 no existe
     */
    public LoginResponseDTO login(LoginDTO loginDTO) {
        try {
            UserModel user = userService.getUserByUsername(loginDTO.getUsername());

            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword_hash())) {
                throw new BadCredentialsException("Las credenciales son incorrectas");
            }

            if (!user.isActive()) {
                throw new BadCredentialsException(
                        "La cuenta está desactivada. Contacte al administrador.");
            }

            // si no fallo enntonces generar el token y retornar la respuesta
            String token = jwtGeneratorService.generateToken(user);
            EmployeeModel employee = employeeRepository.findByUserId(user.getId()).orElse(null);

            UserDTO userDTO = userMapper.userToUserDTO(user, employee);

            return new LoginResponseDTO(
                    userDTO.username(),
                    userDTO.email(),
                    userDTO.active(),
                    token,
                    userDTO.role(),
                    userDTO.employee()
            );

        } catch (NotFoundException e) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

    }
}
