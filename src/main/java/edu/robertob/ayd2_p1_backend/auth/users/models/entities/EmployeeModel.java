package edu.robertob.ayd2_p1_backend.auth.users.models.entities;

import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "employees")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeModel extends BaseModel {

    @Column(nullable = false)
    private String first_name;

    @Column(nullable = false)
    private String last_name;


    @Column(nullable = false)
    private Double hourly_rate;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private UserModel user;

}
