package com.java.sportshub.middlewares;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.java.sportshub.exceptions.UnauthorizedException;
import com.java.sportshub.models.User;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AuthenticatedUser.class)
        && User.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
    HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();

    Object attribute = request.getAttribute(TokenValidationInterceptor.AUTHENTICATED_USER_ATTR);

    if (!(attribute instanceof User user)) {
      throw new UnauthorizedException("Los detalles del usuario autenticado no están disponibles");
    }

    return user;
  }
}
