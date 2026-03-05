package edu.robertob.ayd2_p1_backend.auth.roles.services;

import java.util.Arrays;
import java.util.List;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.roles.repositories.RoleRepository;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.core.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Retorna todos los roles registrados en la base de datos,
     * excluyendo PARTICIPANT (no asignable manualmente).
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
     * Usado internamente para asignar roles fijos como PARTICIPANT.
     *
     * @param code el enum del rol (ej. RolesEnum.PARTICIPANT)
     * @throws NotFoundException si no existe en la base de datos
     */
    public RoleModel findRoleByCode(RolesEnum code) throws NotFoundException {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un rol con el código: " + code.name()));
    }

    public RoleModel findRoleById(Long id) throws NotFoundException {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró un rol con el ID: " + id));
    }

}
