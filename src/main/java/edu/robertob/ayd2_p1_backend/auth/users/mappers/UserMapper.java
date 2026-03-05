package edu.robertob.ayd2_p1_backend.auth.users.mappers;

import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.request.CreateUserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.dto.response.UserMeDTO;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.EmployeeModel;
import edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convierte un DTO de creación de usuario en una entidad {@link edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel}.
     * 
     * Este método realiza un mapeo directo de los campos incluidos en el DTO
     * (username, password, role)
     * hacia la entidad de dominio, dejando sin asignar aquellos campos que deben
     * ser
     * gestionados por el sistema u otros componentes (como auditoría o generación
     * de ID).
     * 
     * <p>
     * <strong>Campos no mapeados automáticamente:</strong>
     * </p>
     * <ul>
     * <li><code>id</code> – generado automáticamente por la base de datos o por
     * lógica de servicio</li>
     * <li><code>createdAt</code>, <code>updatedAt</code> – asignados por
     * {@link edu.robertob.ayd2_p1_backend.core.models.entities.BaseModel} vía auditoría</li>
     * <li><code>deletedAt</code>, <code>desactivatedAt</code> – manejados por
     * lógica de eliminación o inactivación</li>
     * 
     * <li><code>role</code> – debe asignarse en la logica del servicio</li>
     * 
     * <li><code>participant</code> – debe asignarse en la logica del servicio</li>
     * </ul>
     * 
     * Estos campos deben establecerse fuera del mapper, usualmente desde el
     * servicio o el motor de persistencia,
     * por lo tanto es esperado y seguro que MapStruct los ignore.
     * 
     * @param createUserDTO DTO con los datos del nuevo usuario
     * @return entidad {@link edu.robertob.ayd2_p1_backend.auth.users.models.entities.UserModel} con los campos principales mapeados
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public UserModel createUserDtoToUser(CreateUserDTO createUserDTO);

    public UserDTO userToUserDTO(UserModel user);

    public UserDTO userToUserDTO(UserModel user, EmployeeModel employee);

    String map(RoleModel role);
}
