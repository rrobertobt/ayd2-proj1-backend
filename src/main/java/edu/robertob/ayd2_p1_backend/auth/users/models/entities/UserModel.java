package edu.robertob.ayd2_p1_backend.auth.users.models.entities;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "users")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class UserModel extends BaseModel {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column()
    private String password_hash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleModel role;

    public UserModel(Long id, String username, String email, String password, RoleModel role) {
        super(id);
        this.username = username;
        this.password_hash = password;
        this.role = role;
    }

}
