package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.ui.adapter.AdminArtworkAdapter;
import com.example.vagmobile.viewmodel.AdminArtworkViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminArtworksActivity extends AppCompatActivity {

    private AdminArtworkViewModel adminArtworkViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Spinner statusSpinner;
    private AdminArtworkAdapter artworkAdapter;
    private List<Artwork> artworkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_artworks);

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadArtworks(null);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        statusSpinner = findViewById(R.id.statusSpinner);

        // Обработчик изменения фильтра статуса
        statusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String status = (String) parent.getItemAtPosition(position);
                if ("All".equals(status)) {
                    loadArtworks(null);
                } else {
                    loadArtworks(status);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        artworkAdapter = new AdminArtworkAdapter(artworkList, new AdminArtworkAdapter.ArtworkActionListener() {
            @Override
            public void onApprove(Artwork artwork) {
                approveArtwork(artwork.getId());
            }

            @Override
            public void onReject(Artwork artwork) {
                rejectArtwork(artwork.getId());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(artworkAdapter);
    }

    private void observeViewModels() {
        adminArtworkViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(AdminArtworkViewModel.class);

        // Наблюдатель для загрузки публикаций
        adminArtworkViewModel.getArtworksResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

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
                    Toast.makeText(this, "Failed to load artworks: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Наблюдатель для одобрения публикации
        adminArtworkViewModel.getApproveResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Toast.makeText(this, "Artwork approved successfully", Toast.LENGTH_SHORT).show();
                    loadArtworks(getSelectedStatus());
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to approve artwork: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Наблюдатель для отклонения публикации
        adminArtworkViewModel.getRejectResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Toast.makeText(this, "Artwork rejected successfully", Toast.LENGTH_SHORT).show();
                    loadArtworks(getSelectedStatus());
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to reject artwork: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadArtworks(String status) {
        progressBar.setVisibility(View.VISIBLE);
        adminArtworkViewModel.getAdminArtworks(0, 20, status);
    }

    private String getSelectedStatus() {
        String status = (String) statusSpinner.getSelectedItem();
        return "All".equals(status) ? null : status;
    }

    private void approveArtwork(Long artworkId) {
        adminArtworkViewModel.approveArtwork(artworkId);
    }

    private void rejectArtwork(Long artworkId) {
        adminArtworkViewModel.rejectArtwork(artworkId);
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        Artwork artwork = new Artwork();
        
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
        
        Object likesObj = artworkData.get("likes");
        if (likesObj != null) {
            if (likesObj instanceof Double) {
                artwork.setLikes(((Double) likesObj).intValue());
            } else if (likesObj instanceof Integer) {
                artwork.setLikes((Integer) likesObj);
            }
        }
        
        Object viewsObj = artworkData.get("views");
        if (viewsObj != null) {
            if (viewsObj instanceof Double) {
                artwork.setViews(((Double) viewsObj).intValue());
            } else if (viewsObj instanceof Integer) {
                artwork.setViews((Integer) viewsObj);
            }
        }

        // Парсинг пользователя
        Object userObj = artworkData.get("user");
        if (userObj instanceof Map) {
            Map<String, Object> userData = (Map<String, Object>) userObj;
            com.example.vagmobile.model.User user = new com.example.vagmobile.model.User();
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

        return artwork;
    }
}

