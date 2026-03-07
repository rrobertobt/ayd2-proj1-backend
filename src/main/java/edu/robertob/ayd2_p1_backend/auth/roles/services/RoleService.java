package edu.robertob.ayd2_p1_backend.auth.roles.services;

import java.util.List;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.repositories.RoleRepository;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Retorna todos los roles registrados en la base de datos.
     */
    public List<RoleModel> findAllRoles() {
        return roleRepository.findAll()
                .stream()
                .toList();
    }

    /**
     * Busca un rol por su nombre descriptivo (label).
     * Usado al crear usuarios desde el frontend.
     *
     * @param name nombre descriptivo (ej. "Administrador")
     * @throws NotFoundException si no existe un rol con ese nombre
     */
    public RoleModel findRoleByName(String name) throws NotFoundException {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un rol con el nombre: " + name));
    }

    /**
     * Busca un rol por su código técnico (coincide con RolesEnum).
     * Usado internamente para asignar roles fijos.
     *
     * @param code el enum del rol (ej. RolesEnum.SYSTEM_ADMIN)
     * @throws NotFoundException si no existe en la base de datos
     */
    public RoleModel findRoleByCode(RolesEnum code) throws NotFoundException {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un rol con el código: " + code.name()));
    }

    /**
     * Busca un rol por código y retorna null cuando no existe.
     */
    @Transactional(readOnly = true)
    public RoleModel findRoleByCodeOrNull(RolesEnum code) {
        return roleRepository.findByCode(code).orElse(null);
    }

    public RoleModel findRoleById(Long id) throws NotFoundException {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un rol con el ID: " + id));
    }

    /**
     * Crea un rol si no existe por código. Si ya existe, lo devuelve sin cambios.
     */
    public RoleModel createRoleIfNotExists(RolesEnum code, String name, String description) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            RoleModel role = new RoleModel();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }
}
