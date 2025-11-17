package com.java.sportshub.middlewares;

import java.util.Arrays;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.java.sportshub.exceptions.UnauthorizedException;
import com.java.sportshub.models.Role;
import com.java.sportshub.models.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleValidationInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (HttpMethod.OPTIONS.matches(request.getMethod())) {
      return true;
    }

    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    RequiredRoles requiredRoles = resolveRequiredRoles(handlerMethod);

    if (requiredRoles == null) {
      return true;
    }

    String[] allowedRoles = requiredRoles.value();

    if (allowedRoles.length == 0) {
      throw new UnauthorizedException("No roles are authorized for this endpoint");
    }

    Object attribute = request.getAttribute(TokenValidationInterceptor.AUTHENTICATED_USER_ATTR);

    if (!(attribute instanceof User user)) {
      throw new UnauthorizedException("Missing authenticated user information");
    }

    Role userRole = user.getRole();

    if (userRole == null || !StringUtils.hasText(userRole.getRoleName())) {
      throw new UnauthorizedException("User has no role assigned");
    }

    boolean hasAccess = Arrays.stream(allowedRoles)
        .filter(StringUtils::hasText)
        .anyMatch(roleName -> roleName.equalsIgnoreCase(userRole.getRoleName()));

    if (!hasAccess) {
      throw new UnauthorizedException("User role is not authorized for this operation");
    }

    return true;
  }

  private RequiredRoles resolveRequiredRoles(HandlerMethod handlerMethod) {
    RequiredRoles methodAnnotation = handlerMethod.getMethodAnnotation(RequiredRoles.class);
    if (methodAnnotation != null) {
      return methodAnnotation;
    }
    return handlerMethod.getBeanType().getAnnotation(RequiredRoles.class);
  }
}
