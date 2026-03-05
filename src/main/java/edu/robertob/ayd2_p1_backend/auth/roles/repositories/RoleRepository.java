package edu.robertob.ayd2_p1_backend.auth.roles.repositories;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleModel, Long> {
    /**
     * Verifica que un rol exista por ID.
     */
    boolean existsById(Long id);

    /**
     * Busca un rol por su ID.
     */
    Optional<RoleModel> findById(Long id);
    /**
     * Busca un rol por su código técnico (coincide con el enum).
     */
    Optional<RoleModel> findByCode(RolesEnum code);

    /**
     * Busca un rol por su nombre descriptivo legible.
     * Usado al recibir labels desde el frontend.
     */
    Optional<RoleModel> findByName(String name);

    boolean existsByCode(RolesEnum code);
}
