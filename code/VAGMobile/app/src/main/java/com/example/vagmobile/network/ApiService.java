package com.example.vagmobile.network;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Аутентификация
    @POST("vag/api/mobile/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> credentials);

    @POST("vag/api/mobile/auth/register")
    Call<Map<String, Object>> register(@Body Map<String, String> registerRequest);

    // Категории
    @GET("vag/api/mobile/categories")
    Call<Map<String, Object>> getCategories();

    @GET("vag/api/mobile/categories/{id}")
    Call<Map<String, Object>> getCategory(@Path("id") Long id);

    @POST("vag/api/mobile/categories")
    Call<Map<String, Object>> createCategory(
            @Header("Authorization") String authHeader,
            @Body Map<String, String> categoryData);

    @PUT("vag/api/mobile/categories/{id}")
    Call<Map<String, Object>> updateCategory(
            @Header("Authorization") String authHeader,
            @Path("id") Long categoryId,
            @Body Map<String, String> categoryData
    );

    @DELETE("vag/api/mobile/categories/{id}")
    Call<Map<String, Object>> deleteCategory(
            @Header("Authorization") String authHeader,
            @Path("id") Long categoryId);

    @GET("vag/api/mobile/categories/{id}/artworks")
    Call<Map<String, Object>> getCategoryArtworks(
            @Path("id") Long id,
            @Query("page") int page,
            @Query("size") int size
    );

    // Публикации
    @GET("vag/api/mobile/artworks")
    Call<Map<String, Object>> getArtworks(@Query("page") int page, @Query("size") int size);

    @GET("vag/api/mobile/artworks/{id}")
    Call<Map<String, Object>> getArtwork(@Path("id") Long id);

    @Multipart
    @POST("vag/api/mobile/artworks/create")
    Call<Map<String, Object>> createArtwork(
            @Header("Authorization") String authHeader,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("categoryIds") RequestBody categoryIds,
            @Part MultipartBody.Part imageFile
    );

    @POST("vag/api/mobile/artworks/{id}/like")
    Call<Map<String, Object>> likeArtwork(@Header("Authorization") String authHeader, @Path("id") Long id);

    @POST("vag/api/mobile/artworks/{id}/unlike")
    Call<Map<String, Object>> unlikeArtwork(@Header("Authorization") String authHeader, @Path("id") Long id);

    @Multipart
    @POST("vag/api/mobile/artworks/{id}/comment")
    Call<Map<String, Object>> addComment(
            @Header("Authorization") String authHeader,
            @Path("id") Long id,
            @Part("content") RequestBody content
    );

    @GET("vag/api/mobile/artworks/search")
    Call<Map<String, Object>> searchArtworks(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    // Пользователи
    @GET("vag/api/mobile/users")
    Call<Map<String, Object>> getAllUsers();

    @GET("vag/api/mobile/users/artists")
    Call<Map<String, Object>> getArtistsWithArtworks();

    @GET("vag/api/mobile/users/profile")
    Call<Map<String, Object>> getCurrentUserProfile(@Header("Authorization") String authHeader);

    @GET("vag/api/mobile/users/{userId}")
    Call<Map<String, Object>> getUserProfile(@Path("userId") Long userId);

    @GET("vag/api/mobile/users/{userId}/artworks")
    Call<Map<String, Object>> getUserArtworks(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("vag/api/mobile/users/liked/artworks")
    Call<Map<String, Object>> getLikedArtworks(
            @Header("Authorization") String authHeader,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("vag/api/mobile/users/profile/update")
    Call<Map<String, Object>> updateProfile(
            @Header("Authorization") String authHeader,
            @Body Map<String, String> profileData);

    @DELETE("vag/api/mobile/users/artworks/{artworkId}")
    Call<Map<String, Object>> deleteUserArtwork(
            @Header("Authorization") String authHeader,
            @Path("artworkId") Long artworkId);

    // Админские функции
    @GET("vag/api/mobile/admin/artworks")
    Call<Map<String, Object>> getAdminArtworks(
            @Header("Authorization") String authHeader,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("vag/api/mobile/admin/artworks/{id}")
    Call<Map<String, Object>> getArtworkForAdmin(
            @Header("Authorization") String authHeader,
            @Path("id") Long id);

    @POST("vag/api/mobile/admin/artworks/{id}/approve")
    Call<Map<String, Object>> approveArtwork(
            @Header("Authorization") String authHeader,
            @Path("id") Long id);

    @POST("vag/api/mobile/admin/artworks/{id}/reject")
    Call<Map<String, Object>> rejectArtwork(
            @Header("Authorization") String authHeader,
            @Path("id") Long id);

    @GET("vag/api/mobile/admin/artworks/stats")
    Call<Map<String, Object>> getArtworkStats(@Header("Authorization") String authHeader);

    @GET("vag/api/mobile/users/{userId}/artworks/all")
    Call<Map<String, Object>> getAllUserArtworks(
            @Header("Authorization") String authHeader,
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );
    @DELETE("vag/api/mobile/artworks/{id}")
    Call<Map<String, Object>> deleteArtwork(
            @Header("Authorization") String authHeader,
            @Path("id") Long id
    );
}