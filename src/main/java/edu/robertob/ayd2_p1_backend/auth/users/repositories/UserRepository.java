package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import java.util.Optional;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long>, JpaSpecificationExecutor<UserModel> {

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<UserModel> findUserByUsername(String username);
}
