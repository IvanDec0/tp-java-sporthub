package com.java.sportshub.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    private String emailOrUsername;
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String emailOrUsername, String password) {
        this.emailOrUsername = emailOrUsername;
        this.password = password;
    }
}
