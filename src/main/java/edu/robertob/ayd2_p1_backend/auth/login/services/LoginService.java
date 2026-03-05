package edu.robertob.ayd2_p1_backend.auth.login.services;

import edu.robertob.ayd2_p1_backend.auth.jwt.services.JwtGeneratorService;
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
        System.out.println("Intentando autenticar al usuario: " + loginDTO.getUsername());
        try {
            UserModel user = userService.getUserByUsername(loginDTO.getUsername());
            System.out.println("Usuario encontrado: " + user.getUsername());

            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword_hash())) {
                System.out.println("Contraseña incorrecta para el usuario: " + loginDTO.getUsername());
                throw new BadCredentialsException("Las credenciales son incorrectas");
            }

            // si no fallo enntonces generar el token y retornar la respuesta
            String token = jwtGeneratorService.generateToken(user);
            System.out.println("Token generado para el usuario: " + loginDTO.getUsername());
            EmployeeModel employee = employeeRepository.findByUserId(user.getId()).orElse(null);
            return new LoginResponseDTO(user.getUsername(), user.getEmail(), user.isActive(),token, user.getRole(), employee);

        } catch (NotFoundException e) {
            System.out.println("Usuario no encontrado: " + loginDTO.getUsername());
            throw new BadCredentialsException("");
        }

    }
}
