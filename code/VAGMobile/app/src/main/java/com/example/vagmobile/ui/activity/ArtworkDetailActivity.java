package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.Comment;
import com.example.vagmobile.model.User;
import com.example.vagmobile.ui.adapter.CommentAdapter;
import com.example.vagmobile.util.SharedPreferencesHelper;
import com.example.vagmobile.viewmodel.ArtworkViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArtworkDetailActivity extends AppCompatActivity {

    private ArtworkViewModel artworkViewModel;
    private Artwork artwork;
    private Long artworkId;

    private ImageView ivArtwork;
    private TextView tvTitle, tvArtist, tvDescription, tvLikes, tvCategories;
    private Button btnLike, btnComment;
    private EditText etComment;
    private RecyclerView recyclerViewComments;
    private ProgressBar progressBar;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private SharedPreferencesHelper prefs;
    private boolean isLikeInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artwork_detail);

        prefs = new SharedPreferencesHelper(this);
        artworkId = getIntent().getLongExtra("artwork_id", -1);

        if (artworkId == -1) {
            Toast.makeText(this, "Artwork not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadArtwork();
    }

    private void initViews() {
        ivArtwork = findViewById(R.id.ivArtwork);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvDescription = findViewById(R.id.tvDescription);
        tvLikes = findViewById(R.id.tvLikes);
        tvCategories = findViewById(R.id.tvCategories);
        btnLike = findViewById(R.id.btnLike);
        btnComment = findViewById(R.id.btnComment);
        etComment = findViewById(R.id.etComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        progressBar = findViewById(R.id.progressBar);

        btnLike.setOnClickListener(v -> toggleLike());
        btnComment.setOnClickListener(v -> addComment());

        // Скрываем кнопки если пользователь не авторизован
        if (!prefs.isLoggedIn()) {
            btnLike.setVisibility(View.GONE);
            btnComment.setVisibility(View.GONE);
            etComment.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void observeViewModels() {
        artworkViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(ArtworkViewModel.class);

        artworkViewModel.getArtworkResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Map<String, Object> artworkData = (Map<String, Object>) result.get("artwork");
                    if (artworkData != null) {
                        artwork = convertToArtwork(artworkData);
                        updateUI();
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to load artwork: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observer для toggle like
        artworkViewModel.getToggleLikeResult().observe(this, result -> {
            if (result != null && Boolean.TRUE.equals((Boolean) result.get("success"))) {

                if (artwork != null) {
                    boolean newState = !artwork.isLiked();
                    artwork.setLiked(newState);
                    artwork.setLikes(newState ? artwork.getLikes() + 1 : artwork.getLikes() - 1);

                    updateLikeButton();
                    tvLikes.setText(artwork.getLikes() + " лайков");
                }

                loadArtwork(); // на всякий случай

                Toast.makeText(this,
                        artwork.isLiked() ? "Лайк добавлен" : "Лайк убран",
                        Toast.LENGTH_SHORT).show();
            } else {
                String msg = result != null ? (String) result.get("message") : "Ошибка";
                Toast.makeText(this, "Не удалось: " + msg, Toast.LENGTH_SHORT).show();
            }

            // Разблокировка кнопки
            isLikeInProgress = false;
            btnLike.setEnabled(true);
        });
    }

    private void loadArtwork() {
        progressBar.setVisibility(View.VISIBLE);
        artworkViewModel.getArtwork(artworkId);
    }

    private void updateUI() {
        if (artwork == null) return;

        tvTitle.setText(artwork.getTitle());
        tvDescription.setText(artwork.getDescription());
        tvLikes.setText(artwork.getLikes() + " лайков");

        Log.d("LIKE_DEBUG", "=== updateUI() вызван ===");
        Log.d("LIKE_DEBUG", "artwork.isLiked() = " + artwork.isLiked());
        Log.d("LIKE_DEBUG", "artwork.getLikes() = " + artwork.getLikes());

        // Отображение категорий
        if (artwork.hasCategories()) {
            tvCategories.setText(artwork.getCategoriesString());
            tvCategories.setVisibility(View.VISIBLE);
        } else {
            tvCategories.setVisibility(View.GONE);
        }

        // Отображение реального пользователя
        if (artwork.getUser() != null && artwork.getUser().getUsername() != null) {
            tvArtist.setText("By " + artwork.getUser().getUsername());
        } else {
            tvArtist.setText("By Unknown Artist");
        }

        // Загрузка изображения
        if (artwork.getImagePath() != null && !artwork.getImagePath().isEmpty()) {
            String imagePath = artwork.getImagePath();
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }
            String imageUrl = "http://192.168.0.51:8080/vag/uploads/" + imagePath;
            Log.d("ArtworkDetail", "Loading image from URL: " + imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivArtwork);
        } else {
            Log.d("ArtworkDetail", "ImagePath is null or empty");
        }

        // Обновление комментариев
        if (artwork.getComments() != null) {
            commentList.clear();
            commentList.addAll(artwork.getComments());
            commentAdapter.notifyDataSetChanged();
        }

        // Обновление кнопки лайка только для авторизованных пользователей
        if (prefs.isLoggedIn()) {
            updateLikeButton();
        }
    }

    private void updateLikeButton() {
        if (artwork.isLiked()) {
            btnLike.setText("Убрать лайк");
            btnLike.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            btnLike.setText("Поставить лайк");
            btnLike.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    private void toggleLike() {
        if (!prefs.isLoggedIn()) {
            Toast.makeText(this, "Пожалуйста, войдите в систему чтобы ставить лайки", Toast.LENGTH_SHORT).show();
            return;
        }

        if (artwork == null) {
            Toast.makeText(this, "Данные публикации не загружены", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ArtworkDetail", "Toggling like - artworkId: " + artworkId + ", currently liked: " + artwork.isLiked());

        artworkViewModel.toggleLike(artworkId, artwork.isLiked());
    }

    private void addComment() {
        if (!prefs.isLoggedIn()) {
            Toast.makeText(this, "Пожалуйста, войдите в систему чтобы комментировать", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etComment.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите комментарий", Toast.LENGTH_SHORT).show();
            return;
        }

        // Блокируем кнопку до завершения запроса
        btnComment.setEnabled(false);
        artworkViewModel.addComment(artworkId, content);
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
        } else {
            artwork.setLikes(0); // на всякий случай
        }

        artwork.setStatus((String) artworkData.get("status"));

// ВОТ ЭТА СТРОКА — САМАЯ ГЛАВНАЯ! (ты забыла её вставить)
        artwork.setLiked(Boolean.TRUE.equals(artworkData.get("liked")));
        // Парсинг пользователя
        if (artworkData.get("user") != null) {
            Map<String, Object> userData = (Map<String, Object>) artworkData.get("user");
            User user = new User();

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
        } else {
            User unknownUser = new User();
            unknownUser.setUsername("Неизвестный художник");
            artwork.setUser(unknownUser);
        }

        // Конвертируем комментарии
        if (artworkData.get("comments") != null) {
            List<Map<String, Object>> commentsData = (List<Map<String, Object>>) artworkData.get("comments");
            List<Comment> comments = new ArrayList<>();
            for (Map<String, Object> commentData : commentsData) {
                Comment comment = convertToComment(commentData);
                comments.add(comment);
            }
            artwork.setComments(comments);
        }

        // Конвертируем категории
        if (artworkData.get("categories") != null) {
            List<Map<String, Object>> categoriesData = (List<Map<String, Object>>) artworkData.get("categories");
            List<com.example.vagmobile.model.Category> categories = new ArrayList<>();
            for (Map<String, Object> categoryData : categoriesData) {
                com.example.vagmobile.model.Category category = convertToCategory(categoryData);
                categories.add(category);
            }
            artwork.setCategories(categories);
        }

        return artwork;
    }

    private Comment convertToComment(Map<String, Object> commentData) {
        Comment comment = new Comment();

        if (commentData.get("id") != null) {
            if (commentData.get("id") instanceof Double) {
                comment.setId(((Double) commentData.get("id")).longValue());
            } else if (commentData.get("id") instanceof Long) {
                comment.setId((Long) commentData.get("id"));
            }
        }

        comment.setContent((String) commentData.get("content"));

        if (commentData.get("user") != null) {
            Map<String, Object> userData = (Map<String, Object>) commentData.get("user");
            User user = new User();

            if (userData.get("id") != null) {
                if (userData.get("id") instanceof Double) {
                    user.setId(((Double) userData.get("id")).longValue());
                } else if (userData.get("id") instanceof Long) {
                    user.setId((Long) userData.get("id"));
                }
            }

            user.setUsername((String) userData.get("username"));
            comment.setUser(user);
        }

        if (commentData.get("dateCreated") != null) {
            Object dateObj = commentData.get("dateCreated");
            if (dateObj instanceof String) {
                String dateString = (String) dateObj;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    Date date = format.parse(dateString);
                    comment.setDateCreated(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return comment;
    }

    private com.example.vagmobile.model.Category convertToCategory(Map<String, Object> categoryData) {
        com.example.vagmobile.model.Category category = new com.example.vagmobile.model.Category();

        Object idObj = categoryData.get("id");
        if (idObj != null) {
            if (idObj instanceof Double) {
                category.setId(((Double) idObj).longValue());
            } else if (idObj instanceof Integer) {
                category.setId(((Integer) idObj).longValue());
            } else if (idObj instanceof Long) {
                category.setId((Long) idObj);
            }
        }

        category.setName((String) categoryData.get("name"));
        category.setDescription((String) categoryData.get("description"));

        return category;
    }
}