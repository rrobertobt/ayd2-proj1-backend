package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.services.RoleService;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     *
     * @param createUserDTO datos del usuario a crear
     * @return el usuario guardado
     */
    public UserModel createUser(CreateUserDTO createUserDTO) throws NotFoundException {
        UserModel userModel = new UserModel();
        userModel.setUsername(createUserDTO.getUsername());
        userModel.setPassword_hash(passwordEncoder.encode(createUserDTO.getPassword()));

        RoleModel roleToSet = roleService.findRoleById(createUserDTO.getRoleId());
        userModel.setRole(roleToSet);
        return userRepository.save(userModel);
    }


    /**
     * Busca un usuario por su ID.
     * 
     * @param id identificador único del usuario
     * @return el usuario encontrado
     * @throws NotFoundException si no existe un usuario con el ID proporcionado
     */
    public UserModel getUserById(Long id) throws NotFoundException {
        UserModel UserModel = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("No se encontró un usuario con el ID proporcionado."));
        return UserModel;
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * 
     * @param username nombre de usuario a buscar
     * @return el usuario encontrado
     * @throws NotFoundException si no existe un usuario con el nombre proporcionado
     */
    public UserModel getUserByUsername(String username) throws NotFoundException {
        UserModel UserModel = userRepository.findUserByUsername(username).orElseThrow(
                () -> new NotFoundException("No se encontró un usuario con el ID proporcionado."));
        return UserModel;
    }

    public Long count() {
        return userRepository.count();
    }

}
