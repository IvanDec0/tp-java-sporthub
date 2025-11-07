package com.java.sportshub.dtos;

import com.java.sportshub.models.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {
    private Long id;
    private String roleName;

    public RoleDTO() {
    }

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.roleName = role.getRoleName();
    }
}
