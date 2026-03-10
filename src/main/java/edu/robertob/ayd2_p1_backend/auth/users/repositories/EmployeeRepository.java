package edu.robertob.ayd2_p1_backend.auth.users.repositories;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeModel, Long> {

    Optional<EmployeeModel> findByUserId(Long userId);

    Optional<EmployeeModel> findByUserUsername(String username);
}
