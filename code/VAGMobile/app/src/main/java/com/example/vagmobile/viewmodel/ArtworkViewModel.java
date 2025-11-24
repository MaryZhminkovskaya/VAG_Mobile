package com.example.vagmobile.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vagmobile.repository.ArtworkRepository;
import okhttp3.MultipartBody;
import java.util.Map;

public class ArtworkViewModel extends AndroidViewModel {
    private ArtworkRepository artworkRepository;
    private MutableLiveData<Map<String, Object>> artworksResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> artworkResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> createResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> likeResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> unlikeResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> addCommentResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> searchResult = new MutableLiveData<>();

    // Добавьте эти поля
    private MutableLiveData<Map<String, Object>> categoryArtworksResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> likedArtworksResult = new MutableLiveData<>();

    public ArtworkViewModel(Application application) {
        super(application);
        artworkRepository = new ArtworkRepository(application);
    }

    public void getArtworks(int page, int size) {
        artworkRepository.getArtworks(page, size).observeForever(result -> {
            if (result != null) {
                artworksResult.setValue(result);
                // Логируем для отладки
                Log.d("ArtworkViewModel", "Artworks result received: " + result.toString());
            }
        });
    }



    public void getArtwork(Long id) {
        artworkRepository.getArtwork(id).observeForever(result -> {
            artworkResult.setValue(result);
        });
    }

    public void createArtwork(String title, String description, String categoryIds, MultipartBody.Part image) {
        artworkRepository.createArtwork(title, description, categoryIds, image).observeForever(result -> {
            createResult.setValue(result);
        });
    }

    public void likeArtwork(Long id) {
        artworkRepository.likeArtwork(id).observeForever(result -> {
            likeResult.setValue(result);
        });
    }

    public void unlikeArtwork(Long id) {
        artworkRepository.unlikeArtwork(id).observeForever(result -> {
            unlikeResult.setValue(result);
        });
    }

    public void addComment(Long id, String content) {
        artworkRepository.addComment(id, content).observeForever(result -> {
            addCommentResult.setValue(result);
        });
    }

    public void searchArtworks(String query, int page, int size) {
        artworkRepository.searchArtworks(query, page, size).observeForever(result -> {
            searchResult.setValue(result);
        });
    }

//    public void getCategoryArtworks(Long categoryId, int page, int size) {
//        artworkRepository.getCategoryArtworks(categoryId, page, size).observeForever(result -> {
//            categoryArtworksResult.setValue(result);
//        });
//    }

    public void getLikedArtworks(int page, int size) {
        artworkRepository.getLikedArtworks(page, size).observeForever(result -> {
            likedArtworksResult.setValue(result);
        });
    }

    // LiveData геттеры
    public LiveData<Map<String, Object>> getArtworksResult() {
        return artworksResult;
    }

    public LiveData<Map<String, Object>> getArtworkResult() {
        return artworkResult;
    }

    public LiveData<Map<String, Object>> getCreateResult() {
        return createResult;
    }

    public LiveData<Map<String, Object>> getLikeResult() {
        return likeResult;
    }

    public LiveData<Map<String, Object>> getUnlikeResult() {
        return unlikeResult;
    }

    public LiveData<Map<String, Object>> getAddCommentResult() {
        return addCommentResult;
    }

    public LiveData<Map<String, Object>> getSearchResult() {
        return searchResult;
    }

    // Добавьте эти LiveData геттеры
    public LiveData<Map<String, Object>> getCategoryArtworksResult() {
        return categoryArtworksResult;
    }

    public LiveData<Map<String, Object>> getLikedArtworksResult() {
        return likedArtworksResult;
    }
}