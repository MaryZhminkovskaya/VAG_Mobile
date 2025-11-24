package com.example.vagmobile.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.example.vagmobile.network.ApiClient;
import com.example.vagmobile.network.ApiService;
import com.example.vagmobile.util.SharedPreferencesHelper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class ArtworkRepository {
    private ApiService apiService;
    private Context context;

    public ArtworkRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public ArtworkRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private String getAuthHeader() {
        if (context != null) {
            SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
            String token = prefs.getToken();
            if (token != null) {
                return "Bearer " + token;
            }
        }
        return null;
    }

    public MutableLiveData<Map<String, Object>> getArtworks(int page, int size) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        apiService.getArtworks(page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to load artworks: " + response.message());
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

    public MutableLiveData<Map<String, Object>> getArtwork(Long id) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        apiService.getArtwork(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to load artwork");
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

    public MutableLiveData<Map<String, Object>> createArtwork(
            String title,
            String description,
            String categoryIds,
            MultipartBody.Part image) {

        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Создаем RequestBody для текстовых полей
        RequestBody titleBody = RequestBody.create(MultipartBody.FORM, title);
        RequestBody descriptionBody = RequestBody.create(MultipartBody.FORM, description);
        RequestBody categoryIdsBody = RequestBody.create(MultipartBody.FORM, categoryIds);

        String authHeader = getAuthHeader();
        System.out.println("ArtworkRepository: Creating artwork with authHeader: " + authHeader);

        apiService.createArtwork(authHeader, titleBody, descriptionBody, categoryIdsBody, image)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body());
                        } else {
                            Map<String, Object> error = new HashMap<>();
                            error.put("success", false);
                            error.put("message", "Failed to create artwork: " + response.message());
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

    public MutableLiveData<Map<String, Object>> likeArtwork(Long id) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        apiService.likeArtwork(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to like artwork");
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

    public MutableLiveData<Map<String, Object>> unlikeArtwork(Long id) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        apiService.unlikeArtwork(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to unlike artwork");
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

    public MutableLiveData<Map<String, Object>> addComment(Long id, String content) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Создаем RequestBody для комментария
        RequestBody contentBody = RequestBody.create(MultipartBody.FORM, content);

        apiService.addComment(id, contentBody).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to add comment");
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

    public MutableLiveData<Map<String, Object>> searchArtworks(String query, int page, int size) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        apiService.searchArtworks(query, page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Search failed: " + response.message());
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

    public MutableLiveData<Map<String, Object>> getCategoryArtworks(Long categoryId, int page, int size) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        apiService.getCategoryArtworks(categoryId, page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to load category artworks");
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

        apiService.getLikedArtworks(page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Failed to load liked artworks");
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
}