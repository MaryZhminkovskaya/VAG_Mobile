package com.example.vag.controller.mobile;

import com.example.vag.dto.AuthResponse;
import com.example.vag.model.User;
import com.example.vag.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/mobile/auth")
public class MobileAuthController {

    private final UserService userService;

    // Простое хранилище токенов (в продакшене используйте Redis или БД)
    private final Map<String, User> tokenStore = new ConcurrentHashMap<>();
    private final Map<Long, String> userTokenStore = new ConcurrentHashMap<>();

    public MobileAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            System.out.println("=== MOBILE LOGIN ATTEMPT ===");
            System.out.println("Username: " + username);

            User user = userService.authenticate(username, password);

            // Убедитесь, что роль установлена правильно
            System.out.println("User role: " + user.getRole().getName().name());

            String token = UUID.randomUUID().toString();
            tokenStore.put(token, user);
            userTokenStore.put(user.getId(), token);

            System.out.println("Token generated: " + token);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setSuccess(true);
            authResponse.setMessage("Login successful");
            authResponse.setId(user.getId());
            authResponse.setUsername(user.getUsername());
            authResponse.setEmail(user.getEmail());
            authResponse.setRole(user.getRole().getName().name()); // Убедитесь, что это строка

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", authResponse);
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid credentials: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token != null && tokenStore.containsKey(token)) {
                User user = tokenStore.get(token);
                tokenStore.remove(token);
                userTokenStore.remove(user.getId());
                System.out.println("User logged out: " + user.getUsername());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Logout failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token != null && tokenStore.containsKey(token)) {
                User user = tokenStore.get(token);
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("username", user.getUsername());
                response.put("role", user.getRole().getName().name());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", false);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
    }

    // Метод для проверки токена
    public User getUserFromToken(String authHeader) {
        try {
            String token = extractToken(authHeader);
            System.out.println("=== TOKEN VALIDATION ===");
            System.out.println("Token: " + token);
            System.out.println("Token store size: " + tokenStore.size());
            System.out.println("Token store keys: " + tokenStore.keySet());

            if (token != null && tokenStore.containsKey(token)) {
                User user = tokenStore.get(token);
                System.out.println("User found: " + user.getUsername());
                System.out.println("User role: " + user.getRole().getName().name());
                return user;
            } else {
                System.out.println("Token not found in store");
                return null;
            }
        } catch (Exception e) {
            System.out.println("=== TOKEN ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader; // на случай если токен пришел без Bearer
    }
}