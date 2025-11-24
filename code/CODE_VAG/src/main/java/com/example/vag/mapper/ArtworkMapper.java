package com.example.vag.mapper;

import com.example.vag.dto.*;
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
        dto.setStatus(artwork.getStatus());
        dto.setLikes(artwork.getLikes());
        dto.setViews(artwork.getViews());
        dto.setDateCreation(artwork.getDateCreation()); // Используем getDateCreation()
        dto.setLiked(artwork.getLiked()); // Используем getLiked()

        // Преобразование пользователя
        if (artwork.getUser() != null) {
            dto.setUser(toUserDTO(artwork.getUser()));
        }

        // Преобразование категорий
        if (artwork.getCategories() != null) {
            List<CategoryDTO> categoryDTOs = artwork.getCategories().stream()
                    .map(this::toCategoryDTO)
                    .collect(Collectors.toList());
            dto.setCategories(categoryDTOs);
        }

        // Преобразование комментариев
        if (artwork.getComments() != null) {
            List<CommentDTO> commentDTOs = artwork.getComments().stream()
                    .map(this::toCommentDTO)
                    .collect(Collectors.toList());
            dto.setComments(commentDTOs);
        }

        return dto;
    }

    public List<ArtworkDTO> toDTOList(List<Artwork> artworks) {
        return artworks.stream()
                .map(this::toDTO)
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

        if (user.getRole() != null) {
            dto.setRole(user.getRole().getName().name());
        }

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

        if (category.getApprovedArtworksCount() != null) {
            dto.setApprovedArtworksCount(category.getApprovedArtworksCount());
        }

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
        dto.setDateCreated(comment.getDateCreated()); // Используем getDateCreated()

        if (comment.getUser() != null) {
            dto.setUser(toUserDTO(comment.getUser()));
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
        dto.setStatus(artwork.getStatus());
        dto.setLikes(artwork.getLikes());
        dto.setViews(artwork.getViews());
        dto.setDateCreation(artwork.getDateCreation());
        dto.setLiked(artwork.getLiked());

        if (artwork.getUser() != null) {
            dto.setUser(toUserDTO(artwork.getUser()));
        }

        // Не включаем категории и комментарии для упрощенного DTO
        // чтобы избежать циклических ссылок и улучшить производительность

        return dto;
    }

    public List<ArtworkDTO> toSimpleDTOList(List<Artwork> artworks) {
        return artworks.stream()
                .map(this::toSimpleDTO)
                .collect(Collectors.toList());
    }
}