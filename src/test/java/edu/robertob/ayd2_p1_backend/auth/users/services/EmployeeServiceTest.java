package edu.robertob.ayd2_p1_backend.auth.users.services;

import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import edu.robertob.ayd2_p1_backend.auth.users.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployeeIfNotExists_existingEmployee_returnsExistingEmployee() {
        UserModel user = buildUser(1L);
        EmployeeModel existing = buildEmployee(5L, user);
        when(employeeRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        EmployeeModel result = employeeService.createEmployeeIfNotExists(user, "Alice", "Smith", 25.0);

        assertSame(existing, result);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployeeIfNotExists_noEmployee_createsAndSaves() {
        UserModel user = buildUser(2L);
        EmployeeModel newEmp = buildEmployee(6L, user);
        when(employeeRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(employeeRepository.save(any(EmployeeModel.class))).thenReturn(newEmp);

        EmployeeModel result = employeeService.createEmployeeIfNotExists(user, "Bob", "Jones", 30.0);

        assertSame(newEmp, result);
        verify(employeeRepository).save(any(EmployeeModel.class));
    }

    private UserModel buildUser(Long id) {
        UserModel user = new UserModel();
        user.setId(id);
        user.setUsername("user" + id);
        return user;
    }

    private EmployeeModel buildEmployee(Long id, UserModel user) {
        EmployeeModel emp = new EmployeeModel();
        emp.setId(id);
        emp.setUser(user);
        emp.setFirst_name("First");
        emp.setLast_name("Last");
        emp.setHourly_rate(25.0);
        return emp;
    }
}
