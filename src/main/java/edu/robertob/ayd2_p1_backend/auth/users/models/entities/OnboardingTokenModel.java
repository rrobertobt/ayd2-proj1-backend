package edu.robertob.ayd2_p1_backend.auth.users.models.entities;

import edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@Table(name = "onboarding_tokens")
@DynamicUpdate
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class OnboardingTokenModel extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}

