package com.java.sportshub.mappers;

import com.java.sportshub.dtos.UserDTO;
import com.java.sportshub.models.User;

public class UserMapper {
    public static UserDTO toUserDTO(User user) {
        user.setPassword("********");
        return new UserDTO(user);
    }

    public static User toUser(UserDTO userDTO) {
        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setPassword(userDTO.getPassword());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setEmail(userDTO.getEmail());
        // user.setRole(userDTO.getRole());
        return user;
    }
}
