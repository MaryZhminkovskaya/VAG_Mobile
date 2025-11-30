package com.example.vag.controller.mobile;

import com.example.vag.dto.ArtworkDTO;
import com.example.vag.mapper.ArtworkMapper;
import com.example.vag.model.Artwork;
import com.example.vag.model.User;
import com.example.vag.service.ArtworkService;
import com.example.vag.service.UserService;
import com.example.vag.util.FileUploadUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileArtworkController {

    private final ArtworkService artworkService;
    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;
    private final ArtworkMapper artworkMapper;
    private final MobileAuthController mobileAuthController;

    public MobileArtworkController(ArtworkService artworkService, UserService userService,
                                   FileUploadUtil fileUploadUtil, ArtworkMapper artworkMapper,
                                   MobileAuthController mobileAuthController) {
        this.artworkService = artworkService;
        this.userService = userService;
        this.fileUploadUtil = fileUploadUtil;
        this.artworkMapper = artworkMapper;
        this.mobileAuthController = mobileAuthController;
    }

    @GetMapping("/artworks")
    public ResponseEntity<?> getApprovedArtworks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Artwork> artworks = artworkService.findPaginatedApprovedArtworks(pageable);

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
            response.put("message", "Failed to fetch artworks");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/artworks/{id}")
    public ResponseEntity<?> getArtwork(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Artwork artwork = artworkService.findByIdWithComments(id);

            User currentUser = null;
            if (authHeader != null) {
                currentUser = mobileAuthController.getUserFromToken(authHeader);
            }

            boolean isApproved = "APPROVED".equals(artwork.getStatus());
            boolean isAuthor = currentUser != null && artwork.getUser() != null &&
                    currentUser.getId().equals(artwork.getUser().getId());
            boolean isAdmin = currentUser != null && currentUser.hasRole("ADMIN");

            if (!isApproved && !isAuthor && !isAdmin) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied");
                return ResponseEntity.status(403).body(response);
            }

            if (isApproved) {
                artworkService.incrementViews(id);
            }

            if (currentUser != null) {
                boolean isLiked = artworkService.isLikedByUser(artwork, currentUser);
                artwork.setLiked(isLiked);
            }

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

    @PostMapping("/artworks/{id}/like")
    public ResponseEntity<?> likeArtwork(@PathVariable Long id,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = null;
            if (authHeader != null) {
                user = mobileAuthController.getUserFromToken(authHeader);
            }

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

    @PostMapping("/artworks/{id}/unlike")
    public ResponseEntity<?> unlikeArtwork(@PathVariable Long id,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = null;
            if (authHeader != null) {
                user = mobileAuthController.getUserFromToken(authHeader);
            }

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

    @PostMapping("/artworks/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestParam String content,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = null;
            if (authHeader != null) {
                user = mobileAuthController.getUserFromToken(authHeader);
            }

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

    @PostMapping("/artworks/create")
    public ResponseEntity<?> createArtwork(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam List<Long> categoryIds,
            @RequestParam MultipartFile imageFile,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            User currentUser = mobileAuthController.getUserFromToken(authHeader);

            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Authentication required");
                return ResponseEntity.status(401).body(response);
            }

            Artwork artwork = new Artwork();
            artwork.setTitle(title);
            artwork.setDescription(description);

            Artwork savedArtwork = artworkService.createWithCategories(artwork, imageFile, currentUser, categoryIds);
            ArtworkDTO artworkDTO = artworkMapper.toSimpleDTO(savedArtwork);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artwork", artworkDTO);
            response.put("message", "Artwork created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create artwork");
            return ResponseEntity.badRequest().body(response);
        }
    }
}