package com.java.sportshub.dtos;

import com.java.sportshub.models.Role;
import com.java.sportshub.models.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String userName;
    private String password;
    private String phoneNumber;
    private String email;
    private Role role;
    private Long roleId;

    public UserDTO() {
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.password = user.getPassword();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.roleId = user.getRole().getId();
    }
}
