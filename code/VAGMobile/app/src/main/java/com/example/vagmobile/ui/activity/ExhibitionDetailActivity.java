package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.Exhibition;
import com.example.vagmobile.ui.adapter.ArtworkAdapter;
import com.example.vagmobile.util.SharedPreferencesHelper;
import com.example.vagmobile.viewmodel.ExhibitionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExhibitionDetailActivity extends AppCompatActivity {

    private ExhibitionViewModel exhibitionViewModel;
    private Exhibition exhibition;
    private Long exhibitionId;

    private ImageView ivExhibitionImage;
    private TextView tvTitle, tvAuthor, tvAuthorOnly, tvDescription, tvArtworksCount;
    private LinearLayout layoutActions;
    private Button btnEdit, btnDelete;
    private RecyclerView recyclerViewArtworks;
    private ProgressBar progressBar;

    private ArtworkAdapter artworkAdapter;
    private List<Artwork> artworkList = new ArrayList<>();
    private SharedPreferencesHelper prefs;

    // Pagination variables
    private int currentPage = 0;
    private final int PAGE_SIZE = 12;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibition_detail);

        prefs = new SharedPreferencesHelper(this);
        exhibitionId = getIntent().getLongExtra("exhibition_id", -1);

        if (exhibitionId == -1) {
            Toast.makeText(this, "Выставка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadExhibition();
    }

    private void initViews() {
        ivExhibitionImage = findViewById(R.id.ivExhibitionImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvAuthorOnly = findViewById(R.id.tvAuthorOnly);
        tvDescription = findViewById(R.id.tvDescription);
        tvArtworksCount = findViewById(R.id.tvArtworksCount);
        layoutActions = findViewById(R.id.layoutActions);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        recyclerViewArtworks = findViewById(R.id.recyclerViewArtworks);
        progressBar = findViewById(R.id.progressBar);

        btnEdit.setOnClickListener(v -> {
            showEditDialog();
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewArtworks.setLayoutManager(layoutManager);

        artworkAdapter = new ArtworkAdapter(artworkList, new ArtworkAdapter.OnArtworkClickListener() {
            @Override
            public void onArtworkClick(Artwork artwork) {
                Intent intent = new Intent(ExhibitionDetailActivity.this, ArtworkDetailActivity.class);
                intent.putExtra("artwork_id", artwork.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Artwork artwork) {
                // Не используется в просмотре выставки
            }

            @Override
            public void onDeleteClick(Artwork artwork) {
                // Не используется в просмотре выставки
            }
        }, false);

        recyclerViewArtworks.setAdapter(artworkAdapter);

        // Add infinite scroll listener
        recyclerViewArtworks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMorePages) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= PAGE_SIZE) {
                            loadNextPage();
                        }
                    }
                }
            }
        });
    }

    private void observeViewModels() {
        exhibitionViewModel = new ViewModelProvider(this).get(ExhibitionViewModel.class);

        exhibitionViewModel.getExhibitionResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Map<String, Object> exhibitionData = (Map<String, Object>) result.get("exhibition");
                    exhibition = parseExhibitionFromMap(exhibitionData);
                    displayExhibition();

                    // Загружаем работы выставки
                    loadExhibitionArtworks();
                } else {
                    String message = (String) result.get("message");
                    if (message != null) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }
            progressBar.setVisibility(View.GONE);
        });

        exhibitionViewModel.getExhibitionArtworksResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    List<Map<String, Object>> artworksData = (List<Map<String, Object>>) result.get("artworks");

                    // Безопасное преобразование totalPages из Double в Integer
                    Integer totalPages = null;
                    Object totalPagesObj = result.get("totalPages");
                    if (totalPagesObj != null) {
                        if (totalPagesObj instanceof Double) {
                            totalPages = ((Double) totalPagesObj).intValue();
                        } else if (totalPagesObj instanceof Integer) {
                            totalPages = (Integer) totalPagesObj;
                        } else if (totalPagesObj instanceof Long) {
                            totalPages = ((Long) totalPagesObj).intValue();
                        }
                    }

                    // Получаем информацию о текущем пользователе
                    SharedPreferencesHelper prefs = new SharedPreferencesHelper(this);
                    Long currentUserId = prefs.getUserId();
                    boolean isLoggedIn = currentUserId != null;

                    List<Artwork> newArtworks = new ArrayList<>();

                    if (artworksData != null) {
                        for (Map<String, Object> artworkData : artworksData) {
                            Artwork artwork = parseArtworkFromMap(artworkData);
                            if (artwork != null) {
                                // Фильтруем публикации по правилам видимости
                                boolean shouldShow = false;

                                String status = artwork.getStatus();
                                Long artworkUserId = artwork.getUser() != null ? artwork.getUser().getId() : null;

                                if (!isLoggedIn) {
                                    // Неавторизованные пользователи видят только APPROVED
                                    shouldShow = "APPROVED".equals(status);
                                } else {
                                    // Авторизованные пользователи видят APPROVED + свои публикации
                                    shouldShow = "APPROVED".equals(status) ||
                                                (artworkUserId != null && artworkUserId.equals(currentUserId));
                                }

                                if (shouldShow) {
                                    newArtworks.add(artwork);
                                }
                            }
                        }
                    }

                    // Для первой страницы очищаем список, для последующих - добавляем
                    if (currentPage == 0) {
                        artworkList.clear();
                        artworkList.addAll(newArtworks);
                        artworkAdapter.notifyDataSetChanged();
                    } else {
                        artworkAdapter.addItems(newArtworks);
                    }

                    // Проверяем, есть ли еще страницы
                    hasMorePages = totalPages != null && currentPage < totalPages - 1;
                    isLoading = false;
                } else {
                    isLoading = false;
                }
            } else {
                isLoading = false;
            }
        });

        exhibitionViewModel.getUpdateResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                String message = (String) result.get("message");

                if (success != null && success) {
                    Toast.makeText(this, message != null ? message : "Выставка успешно обновлена", Toast.LENGTH_SHORT).show();
                    // Перезагружаем данные выставки
                    loadExhibition();
                } else {
                    Toast.makeText(this, message != null ? message : "Не удалось обновить выставку", Toast.LENGTH_SHORT).show();
                }
            }
        });

        exhibitionViewModel.getDeleteResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                String message = (String) result.get("message");

                if (success != null && success) {
                    Toast.makeText(this, message != null ? message : "Выставка успешно удалена", Toast.LENGTH_SHORT).show();
                    // Закрываем активность
                    finish();
                } else {
                    Toast.makeText(this, message != null ? message : "Не удалось удалить выставку", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadExhibition() {
        progressBar.setVisibility(View.VISIBLE);
        exhibitionViewModel.getExhibition(exhibitionId);
    }

    private void loadExhibitionArtworks() {
        currentPage = 0;
        isLoading = true;
        hasMorePages = true;
        exhibitionViewModel.getExhibitionArtworks(exhibitionId, currentPage, PAGE_SIZE);
    }

    private void loadNextPage() {
        if (!isLoading && hasMorePages) {
            isLoading = true;
            currentPage++;
            exhibitionViewModel.getExhibitionArtworks(exhibitionId, currentPage, PAGE_SIZE);
        }
    }

    private void displayExhibition() {
        if (exhibition == null) return;

        tvTitle.setText(exhibition.getTitle());
        tvDescription.setText(exhibition.getDescription());
        tvArtworksCount.setText("Количество работ: " + exhibition.getArtworksCount());

        if (exhibition.getUser() != null) {
            tvAuthor.setText("Автор: " + exhibition.getUser().getUsername());
        }

        if (exhibition.isAuthorOnly()) {
            tvAuthorOnly.setVisibility(View.VISIBLE);
        } else {
            tvAuthorOnly.setVisibility(View.GONE);
        }

        // Загрузка изображения
        String imageUrl = exhibition.getImageUrl();
        if (imageUrl == null && exhibition.getFirstArtwork() != null) {
            // Преобразуем относительный путь в полный URL
            String relativePath = exhibition.getFirstArtwork().getImagePath();
            if (relativePath != null && !relativePath.startsWith("http")) {
                imageUrl = "http://192.168.0.38:8080/uploads/" + relativePath;
            } else {
                imageUrl = relativePath;
            }
        }

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(ivExhibitionImage);
        } else {
            ivExhibitionImage.setImageResource(R.drawable.placeholder_image);
        }

        // Показываем кнопки действий только если пользователь - владелец выставки
        if (prefs.getUserId() != null &&
            exhibition.getUser() != null &&
            prefs.getUserId().equals(exhibition.getUser().getId())) {
            layoutActions.setVisibility(View.VISIBLE);
        }
    }

    private Exhibition parseExhibitionFromMap(Map<String, Object> data) {
        try {
            Exhibition exhibition = new Exhibition();

            // Безопасный парсинг id
            Object idObj = data.get("id");
            if (idObj != null) {
                long id = 0;
                if (idObj instanceof Double) {
                    id = ((Double) idObj).longValue();
                } else if (idObj instanceof Integer) {
                    id = ((Integer) idObj).longValue();
                } else if (idObj instanceof Long) {
                    id = (Long) idObj;
                } else {
                    id = Long.valueOf(idObj.toString());
                }
                exhibition.setId(id);
            }

            exhibition.setTitle((String) data.get("title"));
            exhibition.setDescription((String) data.get("description"));
            exhibition.setImageUrl((String) data.get("imageUrl"));
            exhibition.setAuthorOnly((Boolean) data.get("authorOnly"));
            // Безопасный парсинг artworksCount
            Object artworksCountObj = data.get("artworksCount");
            if (artworksCountObj != null) {
                int artworksCount = 0;
                if (artworksCountObj instanceof Double) {
                    artworksCount = ((Double) artworksCountObj).intValue();
                } else if (artworksCountObj instanceof Integer) {
                    artworksCount = (Integer) artworksCountObj;
                } else if (artworksCountObj instanceof Long) {
                    artworksCount = ((Long) artworksCountObj).intValue();
                } else {
                    artworksCount = Integer.valueOf(artworksCountObj.toString());
                }
                exhibition.setArtworksCount(artworksCount);
            }

            // Парсинг пользователя
            Map<String, Object> userData = (Map<String, Object>) data.get("user");
            if (userData != null) {
                com.example.vagmobile.model.User user = new com.example.vagmobile.model.User();

                // Безопасный парсинг id пользователя
                Object userIdObj = userData.get("id");
                if (userIdObj != null) {
                    long userId = 0;
                    if (userIdObj instanceof Double) {
                        userId = ((Double) userIdObj).longValue();
                    } else if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    } else {
                        userId = Long.valueOf(userIdObj.toString());
                    }
                    user.setId(userId);
                }

                user.setUsername((String) userData.get("username"));
                user.setEmail((String) userData.get("email"));
                exhibition.setUser(user);
            }

            // Парсинг первой работы
            Map<String, Object> firstArtworkData = (Map<String, Object>) data.get("firstArtwork");
            if (firstArtworkData != null) {
                Artwork firstArtwork = new Artwork();

                // Безопасный парсинг id первой работы
                Object artworkIdObj = firstArtworkData.get("id");
                if (artworkIdObj != null) {
                    long artworkId = 0;
                    if (artworkIdObj instanceof Double) {
                        artworkId = ((Double) artworkIdObj).longValue();
                    } else if (artworkIdObj instanceof Integer) {
                        artworkId = ((Integer) artworkIdObj).longValue();
                    } else if (artworkIdObj instanceof Long) {
                        artworkId = (Long) artworkIdObj;
                    } else {
                        artworkId = Long.valueOf(artworkIdObj.toString());
                    }
                    firstArtwork.setId(artworkId);
                }

                firstArtwork.setTitle((String) firstArtworkData.get("title"));
                firstArtwork.setImagePath((String) firstArtworkData.get("imagePath"));
                exhibition.setFirstArtwork(firstArtwork);
            }

            return exhibition;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Artwork parseArtworkFromMap(Map<String, Object> data) {
        try {
            Artwork artwork = new Artwork();

            // Безопасный парсинг id работы
            Object idObj = data.get("id");
            if (idObj != null) {
                long id = 0;
                if (idObj instanceof Double) {
                    id = ((Double) idObj).longValue();
                } else if (idObj instanceof Integer) {
                    id = ((Integer) idObj).longValue();
                } else if (idObj instanceof Long) {
                    id = (Long) idObj;
                } else {
                    id = Long.valueOf(idObj.toString());
                }
                artwork.setId(id);
            }

            artwork.setTitle((String) data.get("title"));
            artwork.setDescription((String) data.get("description"));
            artwork.setImagePath((String) data.get("imagePath"));
            artwork.setStatus((String) data.get("status"));
            // Безопасный парсинг likes
            Object likesObj = data.get("likes");
            if (likesObj != null) {
                int likes = 0;
                if (likesObj instanceof Double) {
                    likes = ((Double) likesObj).intValue();
                } else if (likesObj instanceof Integer) {
                    likes = (Integer) likesObj;
                } else if (likesObj instanceof Long) {
                    likes = ((Long) likesObj).intValue();
                } else {
                    likes = Integer.valueOf(likesObj.toString());
                }
                artwork.setLikes(likes);
            }

            // Безопасный парсинг views
            Object viewsObj = data.get("views");
            if (viewsObj != null) {
                int views = 0;
                if (viewsObj instanceof Double) {
                    views = ((Double) viewsObj).intValue();
                } else if (viewsObj instanceof Integer) {
                    views = (Integer) viewsObj;
                } else if (viewsObj instanceof Long) {
                    views = ((Long) viewsObj).intValue();
                } else {
                    views = Integer.valueOf(viewsObj.toString());
                }
                artwork.setViews(views);
            }

            // Парсинг пользователя
            Map<String, Object> userData = (Map<String, Object>) data.get("user");
            if (userData != null) {
                com.example.vagmobile.model.User user = new com.example.vagmobile.model.User();

                // Безопасный парсинг id пользователя
                Object userIdObj = userData.get("id");
                if (userIdObj != null) {
                    long userId = 0;
                    if (userIdObj instanceof Double) {
                        userId = ((Double) userIdObj).longValue();
                    } else if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    } else {
                        userId = Long.valueOf(userIdObj.toString());
                    }
                    user.setId(userId);
                }

                user.setUsername((String) userData.get("username"));
                user.setEmail((String) userData.get("email"));
                artwork.setUser(user);
            }

            return artwork;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showEditDialog() {
        if (exhibition == null) return;

        // Создаем layout для диалога
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        // Поле для названия
        EditText etTitle = new EditText(this);
        etTitle.setHint("Название выставки");
        etTitle.setText(exhibition.getTitle());
        layout.addView(etTitle);

        // Поле для описания
        EditText etDescription = new EditText(this);
        etDescription.setHint("Описание выставки");
        etDescription.setText(exhibition.getDescription());
        etDescription.setMinLines(3);
        layout.addView(etDescription);

        // Чекбокс "Только для автора"
        CheckBox cbAuthorOnly = new CheckBox(this);
        cbAuthorOnly.setText("Только автор может добавлять работы");
        cbAuthorOnly.setChecked(exhibition.isAuthorOnly());
        layout.addView(cbAuthorOnly);

        new AlertDialog.Builder(this)
                .setTitle("Редактирование выставки")
                .setView(layout)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    boolean authorOnly = cbAuthorOnly.isChecked();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    exhibitionViewModel.updateExhibition(exhibitionId, title, description, authorOnly);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDeleteConfirmationDialog() {
        if (exhibition == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Удаление выставки")
                .setMessage("Вы действительно хотите удалить выставку \"" + exhibition.getTitle() + "\"? Это действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    exhibitionViewModel.deleteExhibition(exhibitionId);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
