package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.User;
import com.example.vagmobile.ui.adapter.ArtworkAdapter;
import com.example.vagmobile.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryDetailActivity extends AppCompatActivity {

    private CategoryViewModel categoryViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvCategoryName, tvCategoryDescription;
    private ArtworkAdapter artworkAdapter;
    private List<Artwork> artworkList = new ArrayList<>();

    private Long categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);

        // Получаем параметры категории
        categoryId = getIntent().getLongExtra("category_id", -1);
        categoryName = getIntent().getStringExtra("category_name");

        if (categoryId == -1) {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadCategoryArtworks();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvCategoryDescription = findViewById(R.id.tvCategoryDescription);

        // Устанавливаем название категории
        if (categoryName != null) {
            tvCategoryName.setText(categoryName);
        }
    }

    private void setupRecyclerView() {
        artworkAdapter = new ArtworkAdapter(artworkList, artwork -> {
            // Открываем детали публикации
            // Intent intent = new Intent(CategoryDetailActivity.this, ArtworkDetailActivity.class);
            // intent.putExtra("artwork_id", artwork.getId());
            // startActivity(intent);
            Toast.makeText(this, "Opening artwork: " + artwork.getTitle(), Toast.LENGTH_SHORT).show();
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(artworkAdapter);
    }

    private void observeViewModels() {
        categoryViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CategoryViewModel.class);

        // Observer для публикаций категории
        categoryViewModel.getCategoryArtworksResult().observe(this, result -> {
            progressBar.setVisibility(android.view.View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    List<Map<String, Object>> artworksData = (List<Map<String, Object>>) result.get("artworks");
                    if (artworksData != null) {
                        artworkList.clear();
                        for (Map<String, Object> artworkData : artworksData) {
                            Artwork artwork = convertToArtwork(artworkData);
                            artworkList.add(artwork);
                        }
                        artworkAdapter.notifyDataSetChanged();
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to load category artworks: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCategoryArtworks() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        categoryViewModel.getCategoryArtworks(categoryId, 0, 20);
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        Artwork artwork = new Artwork();

        // Безопасное преобразование ID
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

        // Безопасное преобразование лайков
        if (artworkData.get("likes") != null) {
            if (artworkData.get("likes") instanceof Double) {
                artwork.setLikes(((Double) artworkData.get("likes")).intValue());
            } else if (artworkData.get("likes") instanceof Integer) {
                artwork.setLikes((Integer) artworkData.get("likes"));
            } else if (artworkData.get("likes") instanceof Long) {
                artwork.setLikes(((Long) artworkData.get("likes")).intValue());
            }
        }

        // Конвертируем пользователя
        if (artworkData.get("user") != null) {
            Map<String, Object> userData = (Map<String, Object>) artworkData.get("user");
            User user = new User();

            if (userData.get("id") != null) {
                if (userData.get("id") instanceof Double) {
                    user.setId(((Double) userData.get("id")).longValue());
                } else if (userData.get("id") instanceof Long) {
                    user.setId((Long) userData.get("id"));
                }
            }

            user.setUsername((String) userData.get("username"));
            artwork.setUser(user);
        }

        return artwork;
    }
}