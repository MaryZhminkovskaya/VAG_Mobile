package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.User;
import com.example.vagmobile.ui.adapter.ArtworkAdapter;
import com.example.vagmobile.util.SharedPreferencesHelper;
import com.example.vagmobile.viewmodel.ArtworkViewModel;
import com.example.vagmobile.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserArtworksActivity extends AppCompatActivity {

    private ArtworkViewModel artworkViewModel;
    private UserViewModel userViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvTitle;
    private Button btnCreateArtwork;
    private ArtworkAdapter artworkAdapter;
    private List<Artwork> artworkList = new ArrayList<>();
    private Long userId;
    private boolean isOwnProfile;
    private SharedPreferencesHelper prefs;
    private Long deletingArtworkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_artworks);

        prefs = new SharedPreferencesHelper(this);
        userId = getIntent().getLongExtra("user_id", -1);
        isOwnProfile = getIntent().getBooleanExtra("is_own_profile", false);

        Long currentUserId = prefs.getUserId();

        // Если user_id не передан, используем текущего пользователя
        if (userId == -1) {
            userId = currentUserId;
            isOwnProfile = true;
        }

        // Проверяем, свой ли это профиль
        if (!isOwnProfile && currentUserId != null && currentUserId.equals(userId)) {
            isOwnProfile = true;
        }

        Log.d("UserArtworksActivity", "User ID: " + userId + ", isOwnProfile: " + isOwnProfile);

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadUserArtworks();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTitle = findViewById(R.id.tvTitle);
        btnCreateArtwork = findViewById(R.id.btnCreateArtwork);

        if (isOwnProfile) {
            tvTitle.setText("Мои публикации");
            btnCreateArtwork.setVisibility(View.VISIBLE);
            btnCreateArtwork.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateArtworkActivity.class));
            });
        } else {
            tvTitle.setText("Публикации пользователя");
            btnCreateArtwork.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        artworkAdapter = new ArtworkAdapter(artworkList, new ArtworkAdapter.OnArtworkClickListener() {
            @Override
            public void onArtworkClick(Artwork artwork) {
                Intent intent = new Intent(UserArtworksActivity.this, ArtworkDetailActivity.class);
                intent.putExtra("artwork_id", artwork.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Artwork artwork) {
                onEditArtwork(artwork);
            }

            @Override
            public void onDeleteClick(Artwork artwork) {
                onDeleteArtwork(artwork);
            }
        }, isOwnProfile);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(artworkAdapter);
    }

    private void observeViewModels() {
        artworkViewModel = new ViewModelProvider(this).get(ArtworkViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observer для загрузки всех публикаций
        artworkViewModel.getArtworksResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    List<Map<String, Object>> artworksData = (List<Map<String, Object>>) result.get("artworks");
                    if (artworksData != null && !artworksData.isEmpty()) {
                        artworkList.clear();
                        for (Map<String, Object> artworkData : artworksData) {
                            Artwork artwork = convertToArtwork(artworkData);

                            // Фильтруем публикации по user ID
                            if (artwork.getUser() != null &&
                                    artwork.getUser().getId() != null &&
                                    artwork.getUser().getId().equals(userId)) {

                                artworkList.add(artwork);
                            }
                        }
                        artworkAdapter.notifyDataSetChanged();

                        if (artworkList.isEmpty()) {
                            showEmptyState("Нет публикаций");
                        } else {
                            hideEmptyState();
                        }
                    } else {
                        showEmptyState("Нет публикаций");
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Ошибка загрузки: " + message, Toast.LENGTH_SHORT).show();
                    showEmptyState("Ошибка загрузки");
                }
            }
        });

        // Observer для результата удаления
        userViewModel.getDeleteArtworkResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Toast.makeText(this, "Публикация удалена", Toast.LENGTH_SHORT).show();

                    // Удаляем из списка и обновляем адаптер
                    if (deletingArtworkId != null) {
                        List<Artwork> newList = new ArrayList<>();
                        for (Artwork artwork : artworkList) {
                            if (artwork.getId() == null || !artwork.getId().equals(deletingArtworkId)) {
                                newList.add(artwork);
                            }
                        }
                        artworkList.clear();
                        artworkList.addAll(newList);
                        artworkAdapter.notifyDataSetChanged();
                        deletingArtworkId = null;
                    }

                    // Перезагружаем список
                    loadUserArtworks();

                    if (artworkList.isEmpty()) {
                        showEmptyState("Нет публикаций");
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Ошибка удаления: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
        artworkViewModel.getDeleteResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null && Boolean.TRUE.equals((Boolean) result.get("success"))) {
                Toast.makeText(this, "Публикация удалена", Toast.LENGTH_SHORT).show();

                // Перезагружаем список — публикация исчезнет мгновенно
                loadUserArtworks();
            } else {
                String message = result != null ? (String) result.get("message") : "Неизвестная ошибка";
                Toast.makeText(this, "Ошибка удаления: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserArtworks() {
        progressBar.setVisibility(View.VISIBLE);

        // Правильно: наблюдаем за LiveData, а не вызываем метод как будто он возвращает LiveData
        artworkViewModel.getAllUserArtworksResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

            if (result == null) {
                Toast.makeText(this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                showEmptyState("Ошибка загрузки");
                return;
            }

            Boolean success = (Boolean) result.get("success");
            if (success == null || !success) {
                String message = (String) result.get("message");
                Toast.makeText(this, message != null ? message : "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                showEmptyState("Нет публикаций");
                return;
            }

            List<Map<String, Object>> artworksData = (List<Map<String, Object>>) result.get("artworks");
            artworkList.clear();

            if (artworksData != null) {
                for (Map<String, Object> data : artworksData) {
                    Artwork artwork = convertToArtwork(data);
                    artworkList.add(artwork);
                }
            }

            artworkAdapter.notifyDataSetChanged();

            if (artworkList.isEmpty()) {
                showEmptyState("Нет публикаций");
            } else {
                hideEmptyState();
            }
        });

        // Запускаем загрузку
        artworkViewModel.getAllUserArtworks(userId, 0, 100);
    }
    private void showEmptyState(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void onEditArtwork(Artwork artwork) {
        if (!isOwnProfile) {
            Toast.makeText(this, "Вы можете редактировать только свои публикации",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Можно редактировать только публикации со статусами PENDING и REJECTED
        if ("APPROVED".equals(artwork.getStatus())) {
            Toast.makeText(this, "Одобренные публикации нельзя редактировать",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CreateArtworkActivity.class);
        intent.putExtra("artwork_id", artwork.getId());
        intent.putExtra("edit_mode", true);
        startActivity(intent);
    }

    public void onDeleteArtwork(Artwork artwork) {
        if (!isOwnProfile) {
            Toast.makeText(this, "Вы можете удалять только свои публикации", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Удалить публикацию")
                .setMessage("Это действие нельзя отменить. Удалить навсегда?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteArtwork(artwork.getId());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteArtwork(Long artworkId) {
        progressBar.setVisibility(View.VISIBLE);
        artworkViewModel.deleteArtwork(artworkId); // ← вот и всё!
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        Artwork artwork = new Artwork();

        if (artworkData.get("id") != null) {
            if (artworkData.get("id") instanceof Double) {
                artwork.setId(((Double) artworkData.get("id")).longValue());
            } else if (artworkData.get("id") instanceof Long) {
                artwork.setId((Long) artworkData.get("id"));
            } else if (artworkData.get("id") instanceof Integer) {
                artwork.setId(((Integer) artworkData.get("id")).longValue());
            }
        }

        artwork.setTitle((String) artworkData.get("title"));
        artwork.setDescription((String) artworkData.get("description"));
        artwork.setImagePath((String) artworkData.get("imagePath"));
        artwork.setStatus((String) artworkData.get("status"));

        if (artworkData.get("likes") != null) {
            if (artworkData.get("likes") instanceof Double) {
                artwork.setLikes(((Double) artworkData.get("likes")).intValue());
            } else if (artworkData.get("likes") instanceof Integer) {
                artwork.setLikes((Integer) artworkData.get("likes"));
            } else if (artworkData.get("likes") instanceof Long) {
                artwork.setLikes(((Long) artworkData.get("likes")).intValue());
            }
        }

        if (artworkData.get("user") != null && artworkData.get("user") instanceof Map) {
            Map<String, Object> userData = (Map<String, Object>) artworkData.get("user");
            User user = new User();

            if (userData.get("id") != null) {
                if (userData.get("id") instanceof Double) {
                    user.setId(((Double) userData.get("id")).longValue());
                } else if (userData.get("id") instanceof Long) {
                    user.setId((Long) userData.get("id"));
                } else if (userData.get("id") instanceof Integer) {
                    user.setId(((Integer) userData.get("id")).longValue());
                }
            }

            user.setUsername((String) userData.get("username"));
            user.setEmail((String) userData.get("email"));
            artwork.setUser(user);
        }

        return artwork;
    }


}