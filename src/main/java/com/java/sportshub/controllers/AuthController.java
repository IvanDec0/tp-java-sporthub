package com.java.sportshub.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.LoginDTO;
import com.java.sportshub.dtos.UserDTO;
import com.java.sportshub.mappers.UserMapper;
import com.java.sportshub.models.User;
import com.java.sportshub.services.UserService;
import com.java.sportshub.utils.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginDTO credentials,
            HttpServletResponse response) {
        User user = userService.loginUser(credentials);

        String token = jwtUtil.generateToken(user.getId().toString());

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(3600); // 1 hour
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", UserMapper.toUserDTO(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO dto) {
        User newUser = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toUserDTO(newUser));
    }
}
