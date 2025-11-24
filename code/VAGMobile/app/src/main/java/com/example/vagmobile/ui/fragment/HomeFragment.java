package com.example.vagmobile.ui.fragment;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.User;
import com.example.vagmobile.ui.activity.ArtworkDetailActivity;
import com.example.vagmobile.ui.adapter.ArtworkAdapter;
import com.example.vagmobile.viewmodel.ArtworkViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private ArtworkViewModel artworkViewModel;
    private ArtworkAdapter artworkAdapter;
    private List<Artwork> artworkList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // Настройка RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        artworkAdapter = new ArtworkAdapter(artworkList, this::onArtworkClick);
        recyclerView.setAdapter(artworkAdapter);

        // Наблюдаем за данными
        artworkViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(ArtworkViewModel.class);

        artworkViewModel.getArtworksResult().observe(getViewLifecycleOwner(), result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    List<Map<String, Object>> artworksData = (List<Map<String, Object>>) result.get("artworks");
                    if (artworksData != null && !artworksData.isEmpty()) {
                        artworkList.clear();
                        for (Map<String, Object> artworkData : artworksData) {
                            Artwork artwork = convertToArtwork(artworkData);
                            if (artwork != null) {
                                artworkList.add(artwork);
                            }
                        }
                        artworkAdapter.notifyDataSetChanged();

                        // Показываем RecyclerView и скрываем сообщение о пустоте
                        recyclerView.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);

                        Log.d("HomeFragment", "Loaded " + artworkList.size() + " artworks");
                    } else {
                        // Нет данных
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText("No artworks found");
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(getContext(), "Failed to load artworks: " + message, Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Error loading artworks");
                }
            }
        });

        // Загружаем данные
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        artworkViewModel.getArtworks(0, 20);

        return view;
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        try {
            Artwork artwork = new Artwork();

            // ID
            if (artworkData.get("id") != null) {
                artwork.setId(((Number) artworkData.get("id")).longValue());
            }

            // Основные поля
            artwork.setTitle((String) artworkData.get("title"));
            artwork.setDescription((String) artworkData.get("description"));
            artwork.setImagePath((String) artworkData.get("imagePath"));
            artwork.setStatus((String) artworkData.get("status"));

            // Числовые поля
            if (artworkData.get("likes") != null) {
                artwork.setLikes(((Number) artworkData.get("likes")).intValue());
            }
            if (artworkData.get("views") != null) {
                artwork.setViews(((Number) artworkData.get("views")).intValue());
            }

            // Пользователь
            if (artworkData.get("user") != null) {
                Map<String, Object> userData = (Map<String, Object>) artworkData.get("user");
                User user = new User();
                if (userData.get("id") != null) {
                    user.setId(((Number) userData.get("id")).longValue());
                }
                user.setUsername((String) userData.get("username"));
                artwork.setUser(user);
            }

            Log.d("HomeFragment", "Converted artwork: " + artwork.getTitle());
            return artwork;
        } catch (Exception e) {
            Log.e("HomeFragment", "Error converting artwork: " + e.getMessage());
            return null;
        }
    }

    private void onArtworkClick(Artwork artwork) {
        // Обработка клика на публикацию
        Intent intent = new Intent(getContext(), ArtworkDetailActivity.class);
        intent.putExtra("artwork_id", artwork.getId());
        startActivity(intent);
    }
}