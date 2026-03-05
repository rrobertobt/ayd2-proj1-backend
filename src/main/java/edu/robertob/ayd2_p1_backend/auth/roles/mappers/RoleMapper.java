package edu.robertob.ayd2_p1_backend.auth.roles.mappers;

import java.util.List;

import edu.robertob.ayd2_p1_backend.auth.roles.models.dto.response.RoleDTO;
import edu.robertob.ayd2_p1_backend.auth.roles.models.entities.RoleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Mapea la entidad Role al DTO.
     * El campo "label" del DTO recibe el "name" de la entidad (ej. "Administrador").
     */
    @Mapping(source = "name", target = "name")
    RoleDTO roleToRoleDTO(RoleModel role);

    List<RoleDTO> rolesToRoleDTOList(List<RoleModel> roles);
}