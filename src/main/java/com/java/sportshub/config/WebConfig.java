package com.java.sportshub.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.java.sportshub.middlewares.AuthenticatedUserArgumentResolver;
import com.java.sportshub.middlewares.RoleValidationInterceptor;
import com.java.sportshub.middlewares.TokenValidationInterceptor;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final TokenValidationInterceptor tokenValidationInterceptor;
    private final RoleValidationInterceptor roleValidationInterceptor;
    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    public WebConfig(
            TokenValidationInterceptor tokenValidationInterceptor,
            RoleValidationInterceptor roleValidationInterceptor,
            AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver) {
        this.tokenValidationInterceptor = tokenValidationInterceptor;
        this.roleValidationInterceptor = roleValidationInterceptor;
        this.authenticatedUserArgumentResolver = authenticatedUserArgumentResolver;
    }

    @Override
    public void configureContentNegotiation(@NonNull ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .ignoreAcceptHeader(true)
                .defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register")
                .order(0);

        registry.addInterceptor(roleValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register")
                .order(1);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserArgumentResolver);
    }
}