package edu.robertob.ayd2_p1_backend.auth.users.mappers;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO userToUserDTO(UserModel user, EmployeeModel employee) {
        UserDTO.RoleInfoDTO roleDTO = null;
        if (user.getRole() != null) {
            RoleModel r = user.getRole();
            roleDTO = new UserDTO.RoleInfoDTO(r.getId(), r.getCode().name(), r.getName());
        }

        UserDTO.EmployeeDTO employeeDTO = null;
        if (employee != null) {
            employeeDTO = new UserDTO.EmployeeDTO(
                    employee.getId(),
                    employee.getFirst_name(),
                    employee.getLast_name(),
                    employee.getHourly_rate()
            );
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive(),
                user.isOnboardingCompleted(),
                roleDTO,
                employeeDTO
        );
    }

    public UserDTO userToUserDTO(UserModel user) {
        return userToUserDTO(user, null);
    }

    public UserMeDTO userToUserMeDTO(UserModel user, EmployeeModel employee) {
        UserDTO full = userToUserDTO(user, employee);
        return new UserMeDTO(full.id(), full.username(), full.email(), full.active(), full.onboardingCompleted(), full.role(), full.employee());
    }
}
