package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.Category;
import com.example.vagmobile.model.User;
import com.example.vagmobile.viewmodel.ArtworkViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminArtworkDetailActivity extends AppCompatActivity {

    private ArtworkViewModel artworkViewModel;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;
    private Long artworkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_artwork_detail);

        // Получаем artwork_id из intent
        artworkId = getIntent().getLongExtra("artwork_id", -1);

        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.contentLayout);

        if (artworkId != -1) {
            initViewModel();
            loadArtworkDetailsForAdmin(artworkId);
        } else {
            Toast.makeText(this, "Artwork ID not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViewModel() {
        artworkViewModel = new ViewModelProvider(this).get(ArtworkViewModel.class);

        // Наблюдатель для загрузки публикации через админский endpoint
        artworkViewModel.getArtworkForAdminResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Map<String, Object> artworkData = (Map<String, Object>) result.get("artwork");
                    if (artworkData != null) {
                        Artwork artwork = convertToArtwork(artworkData);
                        displayArtworkDetails(artwork);
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to load artwork details: " + message, Toast.LENGTH_SHORT).show();

                    // Если админский endpoint не работает, пробуем обычный
                    if (message != null && message.contains("Access denied")) {
                        loadArtworkWithRegularEndpoint(artworkId);
                    }
                }
            }
        });

        // Наблюдатель для обычного endpoint (резервный)
        artworkViewModel.getArtworkResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Map<String, Object> artworkData = (Map<String, Object>) result.get("artwork");
                    if (artworkData != null) {
                        Artwork artwork = convertToArtwork(artworkData);
                        displayArtworkDetails(artwork);
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to load artwork: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadArtworkDetailsForAdmin(Long artworkId) {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        artworkViewModel.getArtworkForAdmin(artworkId);
    }

    private void loadArtworkWithRegularEndpoint(Long artworkId) {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        artworkViewModel.getArtwork(artworkId);
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        Artwork artwork = new Artwork();

        // Безопасное преобразование ID
        Object idObj = artworkData.get("id");
        if (idObj != null) {
            if (idObj instanceof Double) {
                artwork.setId(((Double) idObj).longValue());
            } else if (idObj instanceof Integer) {
                artwork.setId(((Integer) idObj).longValue());
            } else if (idObj instanceof Long) {
                artwork.setId((Long) idObj);
            }
        }

        artwork.setTitle((String) artworkData.get("title"));
        artwork.setDescription((String) artworkData.get("description"));
        artwork.setImagePath((String) artworkData.get("imagePath"));
        artwork.setStatus((String) artworkData.get("status"));

        // Безопасное преобразование лайков
        Object likesObj = artworkData.get("likes");
        if (likesObj != null) {
            if (likesObj instanceof Double) {
                artwork.setLikes(((Double) likesObj).intValue());
            } else if (likesObj instanceof Integer) {
                artwork.setLikes((Integer) likesObj);
            } else if (likesObj instanceof Long) {
                artwork.setLikes(((Long) likesObj).intValue());
            }
        }

        // Безопасное преобразование просмотров
        Object viewsObj = artworkData.get("views");
        if (viewsObj != null) {
            if (viewsObj instanceof Double) {
                artwork.setViews(((Double) viewsObj).intValue());
            } else if (viewsObj instanceof Integer) {
                artwork.setViews((Integer) viewsObj);
            } else if (viewsObj instanceof Long) {
                artwork.setViews(((Long) viewsObj).intValue());
            }
        }

        // Парсинг пользователя
        if (artworkData.get("user") != null) {
            Map<String, Object> userData = (Map<String, Object>) artworkData.get("user");
            User user = new User();

            // Безопасное преобразование ID пользователя
            Object userIdObj = userData.get("id");
            if (userIdObj != null) {
                if (userIdObj instanceof Double) {
                    user.setId(((Double) userIdObj).longValue());
                } else if (userIdObj instanceof Integer) {
                    user.setId(((Integer) userIdObj).longValue());
                } else if (userIdObj instanceof Long) {
                    user.setId((Long) userIdObj);
                }
            }

            user.setUsername((String) userData.get("username"));
            user.setEmail((String) userData.get("email"));
            artwork.setUser(user);
        }

        // Парсинг категорий
        if (artworkData.get("categories") != null) {
            List<Map<String, Object>> categoriesData = (List<Map<String, Object>>) artworkData.get("categories");
            List<Category> categories = new ArrayList<>();
            for (Map<String, Object> categoryData : categoriesData) {
                Category category = new Category();

                Object categoryIdObj = categoryData.get("id");
                if (categoryIdObj != null) {
                    if (categoryIdObj instanceof Double) {
                        category.setId(((Double) categoryIdObj).longValue());
                    } else if (categoryIdObj instanceof Integer) {
                        category.setId(((Integer) categoryIdObj).longValue());
                    } else if (categoryIdObj instanceof Long) {
                        category.setId((Long) categoryIdObj);
                    }
                }

                category.setName((String) categoryData.get("name"));
                category.setDescription((String) categoryData.get("description"));
                categories.add(category);
            }
            // Если в модели Artwork есть поле categories, установите его
            // artwork.setCategories(categories);
        }

        return artwork;
    }

    private void displayArtworkDetails(Artwork artwork) {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvArtist = findViewById(R.id.tvArtist);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvLikes = findViewById(R.id.tvLikes);
        TextView tvViews = findViewById(R.id.tvViews);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);
        TextView tvUserId = findViewById(R.id.tvUserId);
        ImageView ivArtwork = findViewById(R.id.ivArtwork);

        // ОТЛАДОЧНЫЙ ВЫВОД
        System.out.println("AdminArtworkDetailActivity: Displaying artwork - " + artwork.getTitle());
        System.out.println("AdminArtworkDetailActivity: User object - " + artwork.getUser());
        if (artwork.getUser() != null) {
            System.out.println("AdminArtworkDetailActivity: User ID - " + artwork.getUser().getId());
            System.out.println("AdminArtworkDetailActivity: Username - " + artwork.getUser().getUsername());
            System.out.println("AdminArtworkDetailActivity: Email - " + artwork.getUser().getEmail());
        }
        System.out.println("AdminArtworkDetailActivity: Image Path - " + artwork.getImagePath());
        System.out.println("AdminArtworkDetailActivity: Description - " + artwork.getDescription());

        tvTitle.setText(artwork.getTitle() != null ? artwork.getTitle() : "No Title");
        tvDescription.setText(artwork.getDescription() != null ? artwork.getDescription() : "No description");
        tvStatus.setText("Status: " + (artwork.getStatus() != null ? artwork.getStatus() : "UNKNOWN"));
        tvLikes.setText("Likes: " + artwork.getLikes());
        tvViews.setText("Views: " + artwork.getViews());

        // Детальная информация о пользователе
        if (artwork.getUser() != null) {
            tvArtist.setText("Artist: " + (artwork.getUser().getUsername() != null ? artwork.getUser().getUsername() : "Unknown"));
            tvUserEmail.setText("Email: " + (artwork.getUser().getEmail() != null ? artwork.getUser().getEmail() : "N/A"));
            tvUserId.setText("User ID: " + (artwork.getUser().getId() != null ? artwork.getUser().getId().toString() : "N/A"));
        } else {
            tvArtist.setText("Artist: Unknown");
            tvUserEmail.setText("Email: N/A");
            tvUserId.setText("User ID: N/A");
        }

        // Загрузка изображения
        if (artwork.getImagePath() != null && !artwork.getImagePath().isEmpty()) {
            String imagePath = artwork.getImagePath();
            // Убеждаемся, что путь не начинается с /
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }
            String imageUrl = "http://192.168.0.51:8080/vag/uploads/" + imagePath;
            System.out.println("AdminArtworkDetailActivity: Loading image from URL: " + imageUrl);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_error_image)
                    .into(ivArtwork);
        } else {
            System.out.println("AdminArtworkDetailActivity: ImagePath is null or empty");
            ivArtwork.setImageResource(R.drawable.ic_image_placeholder);
        }
    }
}