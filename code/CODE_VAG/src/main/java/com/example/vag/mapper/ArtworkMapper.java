package com.example.vag.mapper;

import com.example.vag.dto.ArtworkDTO;
import com.example.vag.dto.CategoryDTO;
import com.example.vag.dto.CommentDTO;
import com.example.vag.dto.UserDTO;
import com.example.vag.model.Artwork;
import com.example.vag.model.Category;
import com.example.vag.model.Comment;
import com.example.vag.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArtworkMapper {

    public ArtworkDTO toDTO(Artwork artwork) {
        if (artwork == null) {
            return null;
        }

        ArtworkDTO dto = new ArtworkDTO();
        dto.setId(artwork.getId());
        dto.setTitle(artwork.getTitle());
        dto.setDescription(artwork.getDescription());
        dto.setImagePath(artwork.getImagePath());
        dto.setDateCreation(artwork.getDateCreation());
        dto.setStatus(artwork.getStatus());
        dto.setLikes(artwork.getLikes());
        dto.setViews(artwork.getViews());
        dto.setLiked(artwork.getLiked());

        // Безопасная обработка пользователя
        if (artwork.getUser() != null) {
            UserDTO userDTO = toUserDTO(artwork.getUser());
            dto.setUser(userDTO);
        }

        // Безопасная обработка категорий
        if (artwork.getCategories() != null && !artwork.getCategories().isEmpty()) {
            List<CategoryDTO> categoryDTOs = artwork.getCategories().stream()
                    .map(this::toCategoryDTO)
                    .collect(Collectors.toList());
            dto.setCategories(categoryDTOs);
        }

        // Безопасная обработка комментариев
        if (artwork.getComments() != null && !artwork.getComments().isEmpty()) {
            List<CommentDTO> commentDTOs = artwork.getComments().stream()
                    .map(this::toCommentDTO)
                    .collect(Collectors.toList());
            dto.setComments(commentDTOs);
        }

        return dto;
    }

    public ArtworkDTO toSimpleDTO(Artwork artwork) {
        if (artwork == null) {
            return null;
        }

        ArtworkDTO dto = new ArtworkDTO();
        dto.setId(artwork.getId());
        dto.setTitle(artwork.getTitle());
        dto.setDescription(artwork.getDescription());
        dto.setImagePath(artwork.getImagePath());
        dto.setDateCreation(artwork.getDateCreation());
        dto.setStatus(artwork.getStatus());
        dto.setLikes(artwork.getLikes());
        dto.setViews(artwork.getViews());
        dto.setLiked(artwork.getLiked());

        // Только базовые поля, без вложенных объектов
        return dto;
    }

    public List<ArtworkDTO> toDTOList(List<Artwork> artworks) {
        return artworks.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ArtworkDTO> toSimpleDTOList(List<Artwork> artworks) {
        return artworks.stream()
                .map(this::toSimpleDTO)
                .collect(Collectors.toList());
    }

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().getName().name() : null);

        return dto;
    }

    public CategoryDTO toCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setApprovedArtworksCount(category.getApprovedArtworksCount());

        return dto;
    }

    public List<CategoryDTO> toCategoryDTOList(List<Category> categories) {
        return categories.stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO toCommentDTO(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setDateCreated(comment.getDateCreated());

        if (comment.getUser() != null) {
            dto.setUser(toUserDTO(comment.getUser()));
        }

        return dto;
    }
}