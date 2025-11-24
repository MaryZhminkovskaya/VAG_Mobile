package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
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
    private TextView tvTitle, tvArtist, tvDescription, tvLikes, tvViews;
    private Button btnLike, btnComment;
    private EditText etComment;
    private RecyclerView recyclerViewComments;
    private ProgressBar progressBar;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private SharedPreferencesHelper prefs;

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
        tvViews = findViewById(R.id.tvViews);
        btnLike = findViewById(R.id.btnLike);
        btnComment = findViewById(R.id.btnComment);
        etComment = findViewById(R.id.etComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        progressBar = findViewById(R.id.progressBar);

        btnLike.setOnClickListener(v -> toggleLike());
        btnComment.setOnClickListener(v -> addComment());
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

        artworkViewModel.getLikeResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    loadArtwork(); // Перезагружаем данные
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to like: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        artworkViewModel.getAddCommentResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    etComment.setText("");
                    loadArtwork(); // Перезагружаем данные
                    Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to add comment: " + message, Toast.LENGTH_SHORT).show();
                }
            }
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
        tvLikes.setText(artwork.getLikes() + " likes");
        tvViews.setText(artwork.getViews() + " views");

        if (artwork.getUser() != null) {
            tvArtist.setText("By " + artwork.getUser().getUsername());
        }

        // Загрузка изображения
        if (artwork.getImagePath() != null && !artwork.getImagePath().isEmpty()) {
            // Убеждаемся, что путь не начинается с /
            String imagePath = artwork.getImagePath();
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }
            String imageUrl = "http://192.168.0.51:8080/vag/uploads/" + imagePath;
            System.out.println("ArtworkDetailActivity: Loading image from URL: " + imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivArtwork);
        } else {
            System.out.println("ArtworkDetailActivity: ImagePath is null or empty");
        }

        // Обновление комментариев
        if (artwork.getComments() != null) {
            commentList.clear();
            commentList.addAll(artwork.getComments());
            commentAdapter.notifyDataSetChanged();
        }

        // Обновление кнопки лайка
        updateLikeButton();
    }

    private void updateLikeButton() {
        if (artwork.isLiked()) {
            btnLike.setText("Unlike");
            btnLike.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            btnLike.setText("Like");
            btnLike.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    private void toggleLike() {
        if (!prefs.isLoggedIn()) {
            Toast.makeText(this, "Please login to like artworks", Toast.LENGTH_SHORT).show();
            return;
        }

        if (artwork.isLiked()) {
            artworkViewModel.unlikeArtwork(artworkId);
        } else {
            artworkViewModel.likeArtwork(artworkId);
        }
    }

    private void addComment() {
        if (!prefs.isLoggedIn()) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etComment.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter comment", Toast.LENGTH_SHORT).show();
            return;
        }

        artworkViewModel.addComment(artworkId, content);
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        Artwork artwork = new Artwork();
        artwork.setId(((Double) artworkData.get("id")).longValue());
        artwork.setTitle((String) artworkData.get("title"));
        artwork.setDescription((String) artworkData.get("description"));
        artwork.setImagePath((String) artworkData.get("imagePath"));
        artwork.setLikes(((Double) artworkData.get("likes")).intValue());
        artwork.setViews(((Double) artworkData.get("views")).intValue());

        if (artworkData.get("liked") != null) {
            artwork.setLiked((Boolean) artworkData.get("liked"));
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

        return artwork;
    }

    private Comment convertToComment(Map<String, Object> commentData) {
        Comment comment = new Comment();

        // Безопасное преобразование ID
        if (commentData.get("id") != null) {
            if (commentData.get("id") instanceof Double) {
                comment.setId(((Double) commentData.get("id")).longValue());
            } else if (commentData.get("id") instanceof Long) {
                comment.setId((Long) commentData.get("id"));
            }
        }

        comment.setContent((String) commentData.get("content"));

        // Конвертируем данные пользователя
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

        // Конвертируем дату - здесь нужно быть внимательным с форматом даты
        // Если дата приходит как строка, нужно преобразовать в Date
        if (commentData.get("dateCreated") != null) {
            Object dateObj = commentData.get("dateCreated");
            if (dateObj instanceof String) {
                // Если дата приходит как строка, нужно распарсить ее
                String dateString = (String) dateObj;
                try {
                    // Предполагаем, что дата приходит в формате timestamp или ISO
                    // Настройте парсинг в соответствии с вашим API
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    Date date = format.parse(dateString);
                    comment.setDateCreated(date);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Если не удалось распарсить, оставляем null
                }
            }
        }

        return comment;
    }
}