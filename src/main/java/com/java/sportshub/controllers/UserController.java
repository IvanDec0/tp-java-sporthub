package com.java.sportshub.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.RoleDTO;
import com.java.sportshub.dtos.UserDTO;
import com.java.sportshub.mappers.UserMapper;
import com.java.sportshub.middlewares.AuthenticatedUser;
import com.java.sportshub.middlewares.RequiredRoles;
import com.java.sportshub.models.User;
import com.java.sportshub.services.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredRoles({ "USER", "ADMIN" })
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(UserMapper::toUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticatedUser User authenticatedUser) {
        return ResponseEntity.ok(UserMapper.toUserDTO(authenticatedUser));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(
            @AuthenticatedUser User authenticatedUser,
            @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateOwnProfile(authenticatedUser.getId(), userDTO);
        return ResponseEntity.ok(UserMapper.toUserDTO(updatedUser));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticatedUser User authenticatedUser) {
        userService.deleteUser(authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toUserDTO(user));
    }

    @DeleteMapping("/{id}")
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleDTO roleDTO) {
        User updatedUser = userService.updateUserRole(id, roleDTO.getRoleName());
        return ResponseEntity.ok(UserMapper.toUserDTO(updatedUser));
    }
}
