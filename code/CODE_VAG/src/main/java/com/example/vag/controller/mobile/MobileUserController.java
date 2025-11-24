package com.example.vag.controller.mobile;

import com.example.vag.dto.ArtworkDTO;
import com.example.vag.dto.UserDTO;
import com.example.vag.mapper.ArtworkMapper;
import com.example.vag.model.Artwork;
import com.example.vag.model.User;
import com.example.vag.service.ArtworkService;
import com.example.vag.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile/users")
public class MobileUserController {

    private final UserService userService;
    private final ArtworkService artworkService;
    private final ArtworkMapper artworkMapper;

    public MobileUserController(UserService userService, ArtworkService artworkService, ArtworkMapper artworkMapper) {
        this.userService = userService;
        this.artworkService = artworkService;
        this.artworkMapper = artworkMapper;
    }

    // Получить профиль текущего пользователя
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            UserDTO userDTO = artworkMapper.toUserDTO(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch user profile");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить профиль пользователя по ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDTO userDTO = artworkMapper.toUserDTO(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.notFound().build();
        }
    }

    // Получить публикации пользователя
    @GetMapping("/{userId}/artworks")
    public ResponseEntity<?> getUserArtworks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            User currentUser;
            boolean isOwnProfile = false;

            try {
                currentUser = userService.getCurrentUser();
                isOwnProfile = currentUser != null && currentUser.getId().equals(userId);
            } catch (Exception e) {
                currentUser = null;
            }

            List<Artwork> artworks;
            if (isOwnProfile) {
                // Для владельца профиля показываем все публикации
                artworks = artworkService.findByUserWithDetails(user);
            } else {
                // Для других пользователей показываем только одобренные
                artworks = artworkService.findByUserWithDetails(user).stream()
                        .filter(artwork -> "APPROVED".equals(artwork.getStatus()))
                        .collect(Collectors.toList());
            }

            List<ArtworkDTO> artworkDTOs = artworkMapper.toDTOList(artworks);
            UserDTO userDTO = artworkMapper.toUserDTO(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);
            response.put("artworks", artworkDTOs);
            response.put("isOwnProfile", isOwnProfile);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch user artworks");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить понравившиеся публикации текущего пользователя
    @GetMapping("/liked/artworks")
    public ResponseEntity<?> getLikedArtworks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Artwork> artworkPage = artworkService.findLikedArtworks(user, pageable);

            List<ArtworkDTO> artworkDTOs = artworkMapper.toDTOList(artworkPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artworks", artworkDTOs);
            response.put("totalPages", artworkPage.getTotalPages());
            response.put("currentPage", artworkPage.getNumber());
            response.put("totalItems", artworkPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch liked artworks");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Обновить профиль пользователя
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody User updatedUser) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            // Проверяем уникальность username
            if (!updatedUser.getUsername().equals(currentUser.getUsername())) {
                if (userService.findByUsername(updatedUser.getUsername()).isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Username already exists");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Проверяем уникальность email
            if (!updatedUser.getEmail().equals(currentUser.getEmail())) {
                if (userService.findByEmail(updatedUser.getEmail()).isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Email already exists");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Обновляем только разрешенные поля
            currentUser.setUsername(updatedUser.getUsername());
            currentUser.setEmail(updatedUser.getEmail());

            // Если указан новый пароль
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                currentUser.setPassword(updatedUser.getPassword());
            }

            User savedUser = userService.update(currentUser);
            UserDTO userDTO = artworkMapper.toUserDTO(savedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);
            response.put("message", "Profile updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update profile");
            return ResponseEntity.badRequest().body(response);
        }
    }
}