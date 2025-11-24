package com.example.vag.controller.mobile;

import com.example.vag.dto.ArtworkDTO;
import com.example.vag.mapper.ArtworkMapper;
import com.example.vag.model.Artwork;
import com.example.vag.model.Category;
import com.example.vag.model.User;
import com.example.vag.repository.CategoryRepository;
import com.example.vag.service.ArtworkService;
import com.example.vag.service.UserService;
import com.example.vag.util.FileUploadUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileArtworkController {

    private final ArtworkService artworkService;
    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;
    private final ArtworkMapper artworkMapper;
    private final CategoryRepository categoryRepository;
    private final MobileAuthController mobileAuthController;

    public MobileArtworkController(ArtworkService artworkService, UserService userService,
                                   FileUploadUtil fileUploadUtil, ArtworkMapper artworkMapper,
                                   CategoryRepository categoryRepository, MobileAuthController mobileAuthController) {
        this.artworkService = artworkService;
        this.userService = userService;
        this.fileUploadUtil = fileUploadUtil;
        this.artworkMapper = artworkMapper;
        this.categoryRepository = categoryRepository;
        this.mobileAuthController = mobileAuthController;
    }

    // Получить все одобренные публикации
    @GetMapping("/artworks")
    public ResponseEntity<?> getApprovedArtworks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Artwork> artworks = artworkService.findPaginatedApprovedArtworks(pageable);

            // Преобразуем в DTO
            List<ArtworkDTO> artworkDTOs = artworkMapper.toSimpleDTOList(artworks.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artworks", artworkDTOs);
            response.put("totalPages", artworks.getTotalPages());
            response.put("currentPage", artworks.getNumber());
            response.put("totalItems", artworks.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch artworks: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить публикацию по ID
    @GetMapping("/artworks/{id}")
    public ResponseEntity<?> getArtwork(@PathVariable Long id) {
        try {
            Artwork artwork = artworkService.findByIdWithComments(id);

            // Проверка прав доступа
            User currentUser = null;
            try {
                currentUser = userService.getCurrentUser();
            } catch (Exception e) {
                // Пользователь не аутентифицирован
            }

            boolean isApproved = "APPROVED".equals(artwork.getStatus());
            boolean isAuthor = currentUser != null && currentUser.getId().equals(artwork.getUser().getId());
            boolean isAdmin = currentUser != null && currentUser.hasRole("ADMIN");

            if (!isApproved && !isAuthor && !isAdmin) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied");
                return ResponseEntity.status(403).body(response);
            }

            // Проверяем лайк для аутентифицированного пользователя
            if (currentUser != null) {
                boolean isLiked = artworkService.isLikedByUser(artwork, currentUser);
                artwork.setLiked(isLiked);
            }

            // Преобразуем в DTO
            ArtworkDTO artworkDTO = artworkMapper.toDTO(artwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artwork", artworkDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Artwork not found");
            return ResponseEntity.notFound().build();
        }
    }

    // Создать публикацию
    @PostMapping("/artworks/create")
    public ResponseEntity<?> createArtwork(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam List<Long> categoryIds,
            @RequestParam MultipartFile imageFile,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("=== MOBILE CREATE ARTWORK ===");
        System.out.println("Title: " + title);
        System.out.println("Description: " + description);
        System.out.println("Category IDs: " + categoryIds);
        System.out.println("Image file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
        System.out.println("AuthHeader: " + authHeader);

        try {
            // Используем токен для получения пользователя
            User currentUser = mobileAuthController.getUserFromToken(authHeader);
            
            if (currentUser == null) {
                System.out.println("User not authenticated - token invalid or missing");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }
            
            System.out.println("User authenticated: " + currentUser.getUsername());

            Artwork artwork = new Artwork();
            artwork.setTitle(title);
            artwork.setDescription(description);

            // Используем новый метод с категориями
            Artwork savedArtwork = artworkService.createWithCategories(artwork, imageFile, currentUser, categoryIds);
            ArtworkDTO artworkDTO = artworkMapper.toSimpleDTO(savedArtwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artwork", artworkDTO);
            response.put("message", "Artwork created successfully and pending approval");

            System.out.println("Artwork created successfully with ID: " + savedArtwork.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR creating artwork: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create artwork: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    // Поставить лайк
    @PostMapping("/artworks/{id}/like")
    public ResponseEntity<?> likeArtwork(@PathVariable Long id) {
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            artworkService.likeArtwork(id, user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Liked successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to like artwork");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Убрать лайк
    @PostMapping("/artworks/{id}/unlike")
    public ResponseEntity<?> unlikeArtwork(@PathVariable Long id) {
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            artworkService.unlikeArtwork(id, user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Unliked successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to unlike artwork");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Добавить комментарий
    @PostMapping("/artworks/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestParam String content) {
        try {
            User user = userService.getCurrentUser();
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            artworkService.addComment(id, user, content);
            Artwork artwork = artworkService.findByIdWithComments(id);
            ArtworkDTO artworkDTO = artworkMapper.toDTO(artwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artwork", artworkDTO);
            response.put("message", "Comment added successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to add comment");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Поиск публикаций
    @GetMapping("/artworks/search")
    public ResponseEntity<?> searchArtworks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Artwork> artworks = artworkService.searchApprovedArtworks(query, pageable);

            List<ArtworkDTO> artworkDTOs = artworkMapper.toSimpleDTOList(artworks.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artworks", artworkDTOs);
            response.put("totalPages", artworks.getTotalPages());
            response.put("currentPage", artworks.getNumber());
            response.put("totalItems", artworks.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Search failed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Редактировать публикацию
    @PutMapping("/artworks/{id}")
    public ResponseEntity<?> updateArtwork(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) MultipartFile imageFile) {

        System.out.println("=== MOBILE UPDATE ARTWORK ===");
        System.out.println("Artwork ID: " + id);
        System.out.println("Title: " + title);
        System.out.println("Description: " + description);
        System.out.println("Category IDs: " + categoryIds);

        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            Artwork existingArtwork = artworkService.findByIdWithCategories(id)
                    .orElseThrow(() -> new RuntimeException("Artwork not found"));

            // Проверка прав доступа - только автор или админ может редактировать
            if (!existingArtwork.getUser().getId().equals(currentUser.getId()) &&
                    !currentUser.hasRole("ADMIN")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied. You can only edit your own artworks.");
                return ResponseEntity.status(403).body(response);
            }

            // Обновляем поля, если они предоставлены
            if (title != null && !title.trim().isEmpty()) {
                existingArtwork.setTitle(title.trim());
            }
            if (description != null) {
                existingArtwork.setDescription(description.trim());
            }

            // Обновляем категории, если они предоставлены
            if (categoryIds != null && !categoryIds.isEmpty()) {
                List<Category> categories = categoryRepository.findAllByIds(categoryIds);
                existingArtwork.setCategories(new HashSet<>(categories));
            }

            // Обновляем изображение, если оно предоставлено
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
                String safeFileName = originalFileName
                        .replace(" ", "_")
                        .replaceAll("[^a-zA-Z0-9._-]", "");
                String relativePath = "artwork-images/" + currentUser.getId() + "/" + safeFileName;
                existingArtwork.setImagePath(relativePath);
                fileUploadUtil.saveFile(currentUser.getId(), safeFileName, imageFile);
            }

            // При редактировании статус меняется на PENDING для повторной проверки
            // (кроме случая, когда редактирует админ)
            if (!currentUser.hasRole("ADMIN")) {
                existingArtwork.setStatus(Artwork.ArtworkStatus.PENDING.name());
            }

            Artwork updatedArtwork = artworkService.save(existingArtwork);
            ArtworkDTO artworkDTO = artworkMapper.toSimpleDTO(updatedArtwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artwork", artworkDTO);
            response.put("message", "Artwork updated successfully" + 
                    (!currentUser.hasRole("ADMIN") ? " and pending approval" : ""));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR updating artwork: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update artwork: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Удалить публикацию
    @DeleteMapping("/artworks/{id}")
    public ResponseEntity<?> deleteArtwork(@PathVariable Long id) {
        System.out.println("=== MOBILE DELETE ARTWORK ===");
        System.out.println("Artwork ID: " + id);

        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            Artwork artwork = artworkService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Artwork not found"));

            // Проверка прав доступа - только автор или админ может удалить
            if (!artwork.getUser().getId().equals(currentUser.getId()) &&
                    !currentUser.hasRole("ADMIN")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied. You can only delete your own artworks.");
                return ResponseEntity.status(403).body(response);
            }

            artworkService.delete(artwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Artwork deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR deleting artwork: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete artwork: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}