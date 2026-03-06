package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.users.mappers.UserMapper;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final UserMapper userMapper;

    /**
     * Busca un usuario por su ID.
     */
    public UserModel getUserById(Long id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("No se encontró un usuario con el ID proporcionado."));
    }

    /**
     * Busca un usuario por su nombre de usuario.
     */
    public UserModel getUserByUsername(String username) throws NotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(
                () -> new NotFoundException("No se encontró un usuario con el nombre de usuario: " + username));
    }

    /**
     * Devuelve el DTO del usuario autenticado incluyendo su perfil de empleado.
     */
    public UserDTO getAuthenticatedUserByUsername(String username) throws NotFoundException {
        UserModel user = getUserByUsername(username);
        EmployeeModel employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        return userMapper.userToUserDTO(user, employee);
    }

    /**
     * Devuelve el DTO completo (UserMeDTO) del usuario autenticado.
     */
    public UserMeDTO getMeByUsername(String username) throws NotFoundException {
        UserModel user = getUserByUsername(username);
        EmployeeModel employee = employeeRepository.findByUserId(user.getId()).orElse(null);
        return userMapper.userToUserMeDTO(user, employee);
    }

    public Long count() {
        return userRepository.count();
    }
}
