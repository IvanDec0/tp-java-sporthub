package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.RoleDAO;
import com.java.sportshub.daos.UserDAO;
import com.java.sportshub.dtos.LoginDTO;
import com.java.sportshub.dtos.UserDTO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.UnauthorizedException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Role;
import com.java.sportshub.models.User;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User getUserById(Long id) {
        return userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public User loginUser(LoginDTO credentials) {
        validateLoginCredentials(credentials);

        User foundUser = userDAO.findByEmailOrUserName(credentials.getEmailOrUsername(),
                credentials.getEmailOrUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email or username",
                        credentials.getEmailOrUsername()));

        boolean passwordMatch = passwordEncoder.matches(credentials.getPassword(), foundUser.getPassword());

        if (!passwordMatch) {
            throw new UnauthorizedException("User", "Invalid password");
        }

        return foundUser;
    }

    @Transactional
    public User registerUser(UserDTO dto) {
        validateUserDTO(dto);

        if (userDAO.findByEmail(dto.getEmail()).isPresent()) {
            throw new AttributeExistsException("User", "email", dto.getEmail());
        }

        if (userDAO.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new AttributeExistsException("User", "phone number", dto.getPhoneNumber());
        }

        // Search for default role
        Role defaultRole = roleDAO.findByRoleName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "USER"));

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(defaultRole);

        return userDAO.save(user);
    }

    @Transactional
    public User updateUserRole(Long id, String roleName) {
        User user = getUserById(id);

        Role newRole = roleDAO.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        user.setRole(newRole);
        return userDAO.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(false);
        userDAO.save(user);
    }

    private void validateUserDTO(UserDTO dto) {
        if (dto.getUserName() == null || dto.getUserName().trim().isEmpty()) {
            throw new ValidationException("userName", "User name is required");
        }
        if (dto.getEmail() == null || !dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("email", "Valid email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters");
        }
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("phoneNumber", "Phone number is required");
        }
    }

    private void validateLoginCredentials(LoginDTO credentials) {
        if (credentials.getEmailOrUsername() == null || credentials.getEmailOrUsername().trim().isEmpty()) {
            throw new ValidationException("emailOrUsername", "Email or username is required");
        }
        if (credentials.getPassword() == null || credentials.getPassword().trim().isEmpty()) {
            throw new ValidationException("password", "Password is required");
        }
    }
}
