package com.example.vagmobile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.vagmobile.model.User;
import com.example.vagmobile.repository.UserRepository;
import java.util.Map;

public class UserViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<Map<String, Object>> currentUserResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> userResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> userArtworksResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> likedArtworksResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> updateProfileResult = new MutableLiveData<>();

    private MutableLiveData<Map<String, Object>> artistsResult = new MutableLiveData<>();
    private MutableLiveData<Map<String, Object>> artistsWithArtworksResult = new MutableLiveData<>();

    public UserViewModel() {
        userRepository = new UserRepository();
    }
    public void getAllArtists() {
        userRepository.getAllArtists().observeForever(result -> {
            artistsResult.setValue(result);
        });
    }

    public void getArtistsWithArtworks() {
        userRepository.getAllArtists().observeForever(result -> {
            artistsWithArtworksResult.setValue(result);
        });
    }

    public void getCurrentUser() {
        userRepository.getCurrentUser().observeForever(result -> {
            currentUserResult.setValue(result);
        });
    }

    public void getUser(Long userId) {
        userRepository.getUser(userId).observeForever(result -> {
            userResult.setValue(result);
        });
    }

    public void getUserArtworks(Long userId, int page, int size) {
        // TODO: Implement this method
    }

    public void getLikedArtworks(int page, int size) {
        // TODO: Implement this method
    }

    public void updateProfile(User user) {
        // TODO: Implement this method
    }

    public LiveData<Map<String, Object>> getArtistsResult() {
        return artistsResult;
    }

    public LiveData<Map<String, Object>> getArtistsWithArtworksResult() {
        return artistsWithArtworksResult;
    }

    public LiveData<Map<String, Object>> getCurrentUserResult() {
        return currentUserResult;
    }

    public LiveData<Map<String, Object>> getUserResult() {
        return userResult;
    }

    public LiveData<Map<String, Object>> getUserArtworksResult() {
        return userArtworksResult;
    }

    public LiveData<Map<String, Object>> getLikedArtworksResult() {
        return likedArtworksResult;
    }

    public LiveData<Map<String, Object>> getUpdateProfileResult() {
        return updateProfileResult;
    }
}