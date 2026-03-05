package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import java.util.Optional;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

    public Boolean existsByUsername(String username);

    public Optional<UserModel> findUserByUsername(String username);
}
