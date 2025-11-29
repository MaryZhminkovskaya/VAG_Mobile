package com.example.vag.service.impl;

import com.example.vag.model.Artwork;
import com.example.vag.model.Category;
import com.example.vag.model.User;
import com.example.vag.repository.ArtworkRepository;
import com.example.vag.repository.CategoryRepository;
import com.example.vag.repository.LikeRepository;
import com.example.vag.service.ArtworkService;
import com.example.vag.util.FileUploadUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArtworkServiceImpl implements ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final CategoryRepository categoryRepository;
    private final LikeRepository likeRepository;
    private final FileUploadUtil fileUploadUtil;

    public ArtworkServiceImpl(ArtworkRepository artworkRepository,
                              CategoryRepository categoryRepository,
                              LikeRepository likeRepository,
                              FileUploadUtil fileUploadUtil) {
        this.artworkRepository = artworkRepository;
        this.categoryRepository = categoryRepository;
        this.likeRepository = likeRepository;
        this.fileUploadUtil = fileUploadUtil;
    }

    @Override
    @Transactional
    public Artwork createWithCategories(Artwork artwork, MultipartFile imageFile, User user, List<Long> categoryIds) throws IOException {
        try {
            // Загружаем категории в той же транзакции
            List<Category> categories = categoryRepository.findAllByIds(categoryIds);

            if (categories.isEmpty()) {
                throw new RuntimeException("No valid categories found");
            }

            artwork.setUser(user);
            artwork.getCategories().addAll(categories);
            artwork.setStatus(Artwork.ArtworkStatus.PENDING.name());

            // Сохраняем изображение - используем правильный метод
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = fileUploadUtil.saveFile(user.getId(), imageFile);
                artwork.setImagePath(imagePath);
            }

            return artworkRepository.save(artwork);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create artwork: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Artwork create(Artwork artwork, MultipartFile imageFile, User user) throws IOException {
        if (artwork.getCategoryIds() == null || artwork.getCategoryIds().isEmpty()) {
            throw new RuntimeException("Category IDs are required");
        }
        return createWithCategories(artwork, imageFile, user, artwork.getCategoryIds());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Artwork> findAll() {
        return artworkRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Artwork> findById(Long id) {
        return artworkRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Artwork> findByIdWithCategories(Long id) {
        return artworkRepository.findByIdWithCategories(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Artwork findByIdWithComments(Long id) {
        Artwork artwork = artworkRepository.findByIdWithComments(id);
        if (artwork == null) {
            throw new RuntimeException("Artwork not found with id: " + id);
        }
        return artwork;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findAllPaginated(Pageable pageable) {
        return artworkRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findPaginatedApprovedArtworks(Pageable pageable) {
        return artworkRepository.findByStatusOrderByDateCreationDesc("APPROVED", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findByCategoryId(Long categoryId, Pageable pageable) {
        return artworkRepository.findByCategoryIdAndStatus(categoryId, "APPROVED", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> searchApprovedArtworks(String query, Pageable pageable) {
        return artworkRepository.searchApproved(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Artwork> findByUserWithDetails(User user) {
        return artworkRepository.findByUserWithDetails(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findLikedArtworks(User user, Pageable pageable) {
        return artworkRepository.findLikedArtworks(user.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Artwork> findLikedArtworks(User user) {
        return artworkRepository.findLikedArtworksByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Artwork> findByStatus(String status) {
        return artworkRepository.findByStatus(status);
    }

    // УДАЛИТЕ этот метод - он дублируется
    // @Override
    // @Transactional(readOnly = true)
    // public Long countApprovedArtworksByCategoryId(Long categoryId) {
    //     return artworkRepository.countApprovedArtworksByCategoryId(categoryId);
    // }

    @Override
    @Transactional(readOnly = true)
    public long countArtworksByCategoryId(Long categoryId) {
        Long count = artworkRepository.countByCategoriesId(categoryId);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Artwork artwork, User user) {
        return likeRepository.existsByArtworkAndUser(artwork, user);
    }

    @Override
    @Transactional
    public Artwork save(Artwork artwork) {
        return artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void delete(Artwork artwork) {
        artworkRepository.delete(artwork);
    }

    @Override
    @Transactional
    public void likeArtwork(Long artworkId, User user) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
        // Implementation for like
        artwork.setLikes(artwork.getLikes() + 1);
        artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void unlikeArtwork(Long artworkId, User user) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
        // Implementation for unlike
        artwork.setLikes(Math.max(0, artwork.getLikes() - 1));
        artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void addComment(Long artworkId, User user, String content) {
        // Implementation for adding comment
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
        // Add comment logic here
    }

    @Override
    @Transactional
    public void approveArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
        artwork.setStatus(Artwork.ArtworkStatus.APPROVED.name());
        artworkRepository.save(artwork);
    }

    @Override
    @Transactional
    public void rejectArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
        artwork.setStatus(Artwork.ArtworkStatus.REJECTED.name());
        artworkRepository.save(artwork);
    }

    // Остальные методы интерфейса
    @Override
    @Transactional(readOnly = true)
    public List<Artwork> findByExhibitionId(Long exhibitionId) {
        return artworkRepository.findByExhibitionsId(exhibitionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> getApprovedArtworks(Pageable pageable) {
        return findPaginatedApprovedArtworks(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findAll(Pageable pageable) {
        return artworkRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findByStatus(String status, Pageable pageable) {
        return artworkRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findByUser(User user, Pageable pageable) {
        return artworkRepository.findByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findByUserAndStatus(User user, String status, Pageable pageable) {
        return artworkRepository.findByUserAndStatus(user, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Artwork> findByExhibitionId(Long exhibitionId, Pageable pageable) {
        return artworkRepository.findByExhibitionsId(exhibitionId, pageable);
    }

    // ЕДИНСТВЕННЫЙ метод countApprovedArtworksByCategoryId
    @Override
    @Transactional(readOnly = true)
    public long countApprovedArtworksByCategoryId(Long categoryId) {
        Long count = artworkRepository.countApprovedArtworksByCategoryId(categoryId);
        return count != null ? count : 0L;
    }
}