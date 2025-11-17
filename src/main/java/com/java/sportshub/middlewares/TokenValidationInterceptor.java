package com.java.sportshub.middlewares;

import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.java.sportshub.exceptions.UnauthorizedException;
import com.java.sportshub.models.User;
import com.java.sportshub.services.UserService;
import com.java.sportshub.utils.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

  public static final String AUTHENTICATED_USER_ATTR = "authenticatedUser";

  private final JwtUtil jwtUtil;
  private final UserService userService;

  public TokenValidationInterceptor(JwtUtil jwtUtil, UserService userService) {
    this.jwtUtil = jwtUtil;
    this.userService = userService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    if (!isMappedHandler(handler)) {
      return true;
    }

    String token = resolveToken(request);

    if (!StringUtils.hasText(token)) {
      throw new UnauthorizedException("Missing authentication token");
    }

    if (!jwtUtil.validateToken(token)) {
      throw new UnauthorizedException("Invalid authentication token");
    }

    String userIdPayload = jwtUtil.extractUserId(token);

    Long userId;
    try {
      userId = Long.parseLong(userIdPayload);
    } catch (NumberFormatException ex) {
      throw new UnauthorizedException("Invalid token payload");
    }

    User user = userService.getUserById(userId);

    if (Boolean.FALSE.equals(user.getIsActive())) {
      throw new UnauthorizedException("User is inactive");
    }

    request.setAttribute(AUTHENTICATED_USER_ATTR, user);

    return true;
  }

  private boolean isMappedHandler(Object handler) {
    return handler instanceof HandlerMethod;
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      return header.substring(7);
    }

    if (request.getCookies() != null) {
      return Arrays.stream(request.getCookies())
          .filter(cookie -> "token".equals(cookie.getName()))
          .map(Cookie::getValue)
          .filter(StringUtils::hasText)
          .findFirst()
          .orElse(null);
    }

    return null;
  }
}
