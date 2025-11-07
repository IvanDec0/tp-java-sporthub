package com.java.sportshub.mappers;

import com.java.sportshub.dtos.RoleDTO;
import com.java.sportshub.models.Role;

public class RoleMapper {

  public static RoleDTO toDTO(Role role) {
    return new RoleDTO(role);
  }

  public static Role toEntity(RoleDTO dto) {
    Role role = new Role();
    role.setRoleName(dto.getRoleName());
    return role;
  }

  public static void updateEntity(Role role, RoleDTO dto) {
    if (dto.getRoleName() != null) {
      role.setRoleName(dto.getRoleName());
    }
  }
}
