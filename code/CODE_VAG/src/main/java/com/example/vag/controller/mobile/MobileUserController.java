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
    private final MobileAuthController mobileAuthController;

    public MobileUserController(UserService userService,
                                ArtworkService artworkService,
                                ArtworkMapper artworkMapper,
                                MobileAuthController mobileAuthController) {
        this.userService = userService;
        this.artworkService = artworkService;
        this.artworkMapper = artworkMapper;
        this.mobileAuthController = mobileAuthController;
    }

    // Получить профиль текущего пользователя
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = mobileAuthController.getUserFromToken(authHeader);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            // Загружаем пользователя с коллекциями
            User fullUser = userService.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDTO userDTO = artworkMapper.toUserDTO(fullUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch user profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Обновить профиль пользователя
    @PutMapping("/profile/update")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, String> profileRequest,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = mobileAuthController.getUserFromToken(authHeader);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            String username = profileRequest.get("username");
            String email = profileRequest.get("email");
            String description = profileRequest.get("description");

            // Проверяем уникальность username
            if (username != null && !username.equals(user.getUsername())) {
                if (userService.findByUsername(username).isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Username already exists");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Проверяем уникальность email
            if (email != null && !email.equals(user.getEmail())) {
                if (userService.findByEmail(email).isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Email already exists");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Создаем обновленного пользователя
            User updatedUser = new User();
            updatedUser.setId(user.getId());
            if (username != null && !username.trim().isEmpty()) {
                updatedUser.setUsername(username.trim());
            } else {
                updatedUser.setUsername(user.getUsername());
            }

            if (email != null && !email.trim().isEmpty()) {
                updatedUser.setEmail(email.trim());
            } else {
                updatedUser.setEmail(user.getEmail());
            }

            if (description != null) {
                updatedUser.setDescription(description.trim());
            } else {
                updatedUser.setDescription(user.getDescription());
            }

            User savedUser = userService.update(updatedUser);
            UserDTO userDTO = artworkMapper.toUserDTO(savedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);
            response.put("message", "Profile updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update profile: " + e.getMessage());
            e.printStackTrace();
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
            response.put("message", "User not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Получить публикации пользователя
    @GetMapping("/{userId}/artworks")
    public ResponseEntity<?> getUserArtworks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            User currentUser = mobileAuthController.getUserFromToken(authHeader);

            // ВАЖНОЕ ИСПРАВЛЕНИЕ: Всегда показываем ВСЕ публикации пользователя
            // независимо от того, свой это профиль или чужой
            List<Artwork> artworks = artworkService.findByUserWithDetails(user);

            // Альтернативный вариант: показывать все, кроме REJECTED
            // List<Artwork> artworks = artworkService.findByUserWithDetails(user).stream()
            //         .filter(artwork -> !"REJECTED".equals(artwork.getStatus()))
            //         .collect(Collectors.toList());

            List<ArtworkDTO> artworkDTOs = artworkMapper.toSimpleDTOList(artworks);
            UserDTO userDTO = artworkMapper.toUserDTO(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);
            response.put("artworks", artworkDTOs);
            response.put("totalItems", artworks.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch user artworks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить понравившиеся публикации текущего пользователя
    @GetMapping("/liked/artworks")
    public ResponseEntity<?> getLikedArtworks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            User user = mobileAuthController.getUserFromToken(authHeader);

            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Artwork> artworkPage = artworkService.findLikedArtworks(user, pageable);

            List<ArtworkDTO> artworkDTOs = artworkMapper.toSimpleDTOList(artworkPage.getContent());

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
            response.put("message", "Failed to fetch liked artworks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить случайных художников
    @GetMapping("/artists/random")
    public ResponseEntity<?> getRandomArtists(@RequestParam(defaultValue = "4") int count) {
        try {
            List<User> randomArtists = userService.findRandomArtists(count);
            List<UserDTO> userDTOs = randomArtists.stream()
                    .map(artworkMapper::toUserDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artists", userDTOs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch random artists: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить всех пользователей с количеством публикаций
    @GetMapping("/artists")
    public ResponseEntity<?> getAllArtists() {
        try {
            List<User> artists = userService.findAllWithArtworksCount();
            List<UserDTO> userDTOs = artworkMapper.toArtistsWithCountDTOList(artists);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", userDTOs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch artists: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Удалить публикацию пользователя
    @DeleteMapping("/artworks/{artworkId}")
    public ResponseEntity<?> deleteUserArtwork(@PathVariable Long artworkId,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = mobileAuthController.getUserFromToken(authHeader);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            Artwork artwork = artworkService.findById(artworkId)
                    .orElseThrow(() -> new RuntimeException("Artwork not found"));

            // Проверяем, что пользователь является владельцем публикации
            if (!artwork.getUser().getId().equals(user.getId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You can only delete your own artworks");
                return ResponseEntity.status(403).body(response);
            }

            artworkService.delete(artwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Artwork deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete artwork: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }
}