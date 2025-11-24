package com.example.vagmobile.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.vagmobile.model.User;
import com.example.vagmobile.network.ApiClient;
import com.example.vagmobile.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private ApiService apiService;

    public UserRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public MutableLiveData<Map<String, Object>> getCurrentUser() {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Используем существующий метод getCurrentUserProfile
        apiService.getCurrentUserProfile().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to get user profile: " + response.message());
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Network error: " + t.getMessage());
                result.setValue(error);
            }
        });

        return result;
    }

    public MutableLiveData<Map<String, Object>> getUser(Long userId) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Используем существующий метод getUserProfile
        apiService.getUserProfile(userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to get user: " + response.message());
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Network error: " + t.getMessage());
                result.setValue(error);
            }
        });

        return result;
    }

    public MutableLiveData<Map<String, Object>> getUserArtworks(Long userId, int page, int size) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Используем существующий метод getUserArtworks
        apiService.getUserArtworks(userId, page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to get user artworks: " + response.message());
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Network error: " + t.getMessage());
                result.setValue(error);
            }
        });

        return result;
    }

    public MutableLiveData<Map<String, Object>> getLikedArtworks(int page, int size) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Используем существующий метод getLikedArtworks
        apiService.getLikedArtworks(page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to get liked artworks: " + response.message());
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Network error: " + t.getMessage());
                result.setValue(error);
            }
        });

        return result;
    }

    public MutableLiveData<Map<String, Object>> updateProfile(User user) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Для updateProfile нужно добавить метод в ApiService или использовать другой подход
        // Временно возвращаем ошибку
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Update profile method not implemented in ApiService");
        result.setValue(error);

        return result;
    }
}