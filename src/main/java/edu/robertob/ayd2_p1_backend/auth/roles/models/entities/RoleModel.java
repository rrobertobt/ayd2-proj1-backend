package edu.robertob.ayd2_p1_backend.auth.roles.models.entities;

import edu.robertob.ayd2_p1_backend.auth.users.enums.RolesEnum;
import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleModel extends BaseModel {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RolesEnum code;


    @Column(nullable = false, length = 100)
    private String name;

    @Column()
    private String description;

    public RoleModel(Long id, RolesEnum code, String name, String description) {
        super(id);
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * Convierte este rol de base de datos de vuelta al enum de Spring Security.
     * Usado por {@link edu.robertob.ayd2_p1_backend.auth.jwt.services.JwtGeneratorService}
     * y {@link edu.robertob.ayd2_p1_backend.auth.jwt.filter.JwtAuthenticationFilter}.
     */
    public RolesEnum toRolesEnum() {
        return this.code;
    }
}
