package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.OnboardingTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingTokenRepository extends JpaRepository<OnboardingTokenModel, Long> {

    Optional<OnboardingTokenModel> findByTokenAndUsedFalse(String token);

    void deleteByUserId(Long userId);
}

