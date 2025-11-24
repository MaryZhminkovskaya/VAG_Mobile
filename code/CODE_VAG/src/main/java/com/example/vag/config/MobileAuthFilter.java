package com.example.vag.config;

import com.example.vag.controller.mobile.MobileAuthController;
import com.example.vag.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class MobileAuthFilter extends GenericFilterBean {

    private final MobileAuthController mobileAuthController;

    public MobileAuthFilter(MobileAuthController mobileAuthController) {
        this.mobileAuthController = mobileAuthController;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Проверяем только мобильные API endpoints
        if (path.startsWith("/api/mobile/")) {
            String authHeader = httpRequest.getHeader("Authorization");

            System.out.println("=== MOBILE AUTH FILTER ===");
            System.out.println("Path: " + path);
            System.out.println("Auth Header: " + authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Прямая проверка токена без AuthenticationManager
                User user = mobileAuthController.getUserFromToken("Bearer " + token);

                if (user != null) {
                    System.out.println("User authenticated directly: " + user.getUsername());
                    System.out.println("User role: " + user.getRole().getName().name());

                    // Создаем аутентификацию напрямую
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    token,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().name()))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set successfully");
                } else {
                    System.out.println("Token validation failed");
                    if (requiresAuthentication(path)) {
                        handleUnauthorized(httpResponse, "Invalid token");
                        return;
                    }
                }
            } else if (requiresAuthentication(path)) {
                System.out.println("No auth header for protected endpoint");
                handleUnauthorized(httpResponse, "Authentication required");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean requiresAuthentication(String path) {
        return path.startsWith("/api/mobile/artworks/create") ||
                path.startsWith("/api/mobile/artworks/") &&
                        (path.contains("/like") || path.contains("/unlike") || path.contains("/comment")) ||
                path.startsWith("/api/mobile/users/profile") ||
                path.startsWith("/api/mobile/users/liked") ||
                path.startsWith("/api/mobile/admin/") ||
                (path.startsWith("/api/mobile/categories") &&
                        !path.equals("/api/mobile/categories") &&
                        !path.matches("/api/mobile/categories/\\d+/artworks"));
    }

    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}