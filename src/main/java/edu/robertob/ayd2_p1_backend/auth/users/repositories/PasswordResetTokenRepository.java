package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.PasswordResetTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenModel, Long> {

    Optional<PasswordResetTokenModel> findByTokenAndUsedFalse(String token);

    void deleteByUserId(Long userId);
}
