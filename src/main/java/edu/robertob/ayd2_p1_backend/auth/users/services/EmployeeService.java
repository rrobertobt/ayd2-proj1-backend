package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * Crea el perfil de empleado para el usuario cuando no existe.
     * Si ya existe, devuelve el perfil existente.
     */
    public EmployeeModel createEmployeeIfNotExists(
            UserModel user,
            String firstName,
            String lastName,
            Double hourlyRate
    ) {
        return employeeRepository.findByUserId(user.getId()).orElseGet(() -> {
            EmployeeModel employee = new EmployeeModel();
            employee.setUser(user);
            employee.setFirst_name(firstName);
            employee.setLast_name(lastName);
            employee.setHourly_rate(hourlyRate);
            return employeeRepository.save(employee);
        });
    }
}
