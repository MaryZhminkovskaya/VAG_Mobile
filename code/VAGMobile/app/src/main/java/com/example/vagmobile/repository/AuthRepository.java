package com.example.vagmobile.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.vagmobile.model.AuthResponse;
import com.example.vagmobile.model.User;
import com.example.vagmobile.network.ApiClient;
import com.example.vagmobile.network.ApiService;
import com.example.vagmobile.util.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private ApiService apiService;
    private Context context;

    public AuthRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public MutableLiveData<Map<String, Object>> login(String username, String password) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Создаем JSON запрос
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        System.out.println("Attempting login with JSON: " + loginRequest);

        apiService.login(loginRequest).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                System.out.println("Response code: " + response.code());
                System.out.println("Response body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    Boolean success = (Boolean) responseBody.get("success");

                    System.out.println("Login success: " + success);

                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("success", success != null ? success : false);
                    responseMap.put("message", responseBody.get("message"));

                    if (success != null && success) {
                        try {
                            // Создаем AuthResponse из данных ответа
                            AuthResponse authResponse = new AuthResponse();
                            authResponse.setSuccess(true);
                            authResponse.setMessage((String) responseBody.get("message"));

                            // Извлекаем данные пользователя
                            // Сервер возвращает user как Map (после JSON десериализации)
                            Object userObj = responseBody.get("user");
                            if (userObj instanceof Map) {
                                Map<String, Object> userData = (Map<String, Object>) userObj;
                                if (userData.get("id") != null) {
                                    authResponse.setId(((Number) userData.get("id")).longValue());
                                }
                                if (userData.get("username") != null) {
                                    authResponse.setUsername((String) userData.get("username"));
                                }
                                if (userData.get("email") != null) {
                                    authResponse.setEmail((String) userData.get("email"));
                                }
                                if (userData.get("role") != null) {
                                    authResponse.setRole((String) userData.get("role"));
                                }
                            } else {
                                // Fallback: если user не Map, пытаемся извлечь напрямую из responseBody
                                if (responseBody.get("id") != null) {
                                    authResponse.setId(((Number) responseBody.get("id")).longValue());
                                }
                                if (responseBody.get("username") != null) {
                                    authResponse.setUsername((String) responseBody.get("username"));
                                }
                                if (responseBody.get("email") != null) {
                                    authResponse.setEmail((String) responseBody.get("email"));
                                }
                                if (responseBody.get("role") != null) {
                                    authResponse.setRole((String) responseBody.get("role"));
                                }
                            }
                            
                            // Проверяем, что все необходимые данные получены
                            if (authResponse.getId() == null || authResponse.getUsername() == null) {
                                System.out.println("Warning: Incomplete user data in response");
                                System.out.println("Response body: " + responseBody);
                            }

                            responseMap.put("user", authResponse);

                            // СОХРАНЯЕМ ДАННЫЕ ПОЛЬЗОВАТЕЛЯ В SharedPreferences
                            if (context != null) {
                                SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
                                
                                // Сохраняем токен, если он есть в ответе
                                Object tokenObj = responseBody.get("token");
                                if (tokenObj != null) {
                                    String token = tokenObj.toString();
                                    prefs.saveToken(token);
                                    System.out.println("Token saved: " + token);
                                }
                                
                                if (authResponse.getId() != null && authResponse.getUsername() != null) {
                                    prefs.saveUserData(
                                            authResponse.getId(),
                                            authResponse.getUsername(),
                                            authResponse.getEmail(),
                                            authResponse.getRole()
                                    );
                                    System.out.println("User data saved: " + authResponse.getUsername());
                                } else {
                                    System.out.println("Warning: User data incomplete, not saving");
                                }
                            } else {
                                System.out.println("Error: Context is null, cannot save user data");
                            }
                        } catch (Exception e) {
                            System.out.println("Error processing login response: " + e.getMessage());
                            e.printStackTrace();
                            responseMap.put("success", false);
                            responseMap.put("message", "Error processing login: " + e.getMessage());
                        }
                    }

                    result.setValue(responseMap);
                } else {
                    System.out.println("Login failed with code: " + response.code());
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Login failed: " + response.message() + ". Code: " + response.code());
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                System.out.println("Network error: " + t.getMessage());
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Network error: " + t.getMessage());
                result.setValue(error);
            }
        });

        return result;
    }

    public MutableLiveData<Map<String, Object>> register(User user) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        // Создаем JSON запрос
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", user.getUsername());
        registerRequest.put("email", user.getEmail());
        registerRequest.put("password", user.getPassword());

        System.out.println("Attempting registration with JSON: " + registerRequest);

        apiService.register(registerRequest).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                System.out.println("Registration response code: " + response.code());
                System.out.println("Registration response body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> responseBody = response.body();
                    Boolean success = (Boolean) responseBody.get("success");

                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("success", success != null ? success : false);
                    responseMap.put("message", responseBody.get("message"));

                    if (success != null && success) {
                        // Создаем AuthResponse из данных ответа
                        AuthResponse authResponse = new AuthResponse();
                        authResponse.setSuccess(true);
                        authResponse.setMessage((String) responseBody.get("message"));

                        // Извлекаем данные пользователя
                        if (responseBody.get("id") != null) {
                            authResponse.setId(((Number) responseBody.get("id")).longValue());
                        }
                        if (responseBody.get("username") != null) {
                            authResponse.setUsername((String) responseBody.get("username"));
                        }
                        if (responseBody.get("email") != null) {
                            authResponse.setEmail((String) responseBody.get("email"));
                        }
                        if (responseBody.get("role") != null) {
                            authResponse.setRole((String) responseBody.get("role"));
                        }

                        responseMap.put("user", authResponse);

                        // СОХРАНЯЕМ ДАННЫЕ ПОЛЬЗОВАТЕЛЯ В SharedPreferences
                        SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
                        prefs.saveUserData(
                                authResponse.getId(),
                                authResponse.getUsername(),
                                authResponse.getEmail(),
                                authResponse.getRole()
                        );
                    }

                    result.setValue(responseMap);
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Registration failed: " + response.message() + ". Code: " + response.code());
                    result.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                System.out.println("Registration network error: " + t.getMessage());
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Network error: " + t.getMessage());
                result.setValue(error);
            }
        });

        return result;
    }
}