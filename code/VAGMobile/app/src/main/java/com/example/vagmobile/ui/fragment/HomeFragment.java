package com.example.vagmobile.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import com.example.vagmobile.model.User;
import com.example.vagmobile.ui.activity.ArtworkDetailActivity;
import com.example.vagmobile.ui.activity.ArtistArtworksActivity;
import com.example.vagmobile.ui.activity.MainActivity;
import com.example.vagmobile.ui.adapter.ArtworkAdapter;
import com.example.vagmobile.ui.adapter.ArtistsAdapter;
import com.example.vagmobile.viewmodel.ArtworkViewModel;
import com.example.vagmobile.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private ArtworkViewModel artworkViewModel;
    private UserViewModel userViewModel;

    private RecyclerView rvFeaturedArtworks, rvFeaturedArtists;
    private ArtworkAdapter featuredArtworkAdapter;
    private ArtistsAdapter featuredArtistsAdapter;
    private List<Artwork> featuredArtworks = new ArrayList<>();
    private List<User> featuredArtists = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvArtworksEmpty, tvArtistsEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerViews();
        loadFeaturedContent();

        return view;
    }

    private void initViews(View view) {
        rvFeaturedArtworks = view.findViewById(R.id.rvFeaturedArtworks);
        rvFeaturedArtists = view.findViewById(R.id.rvFeaturedArtists);
        progressBar = view.findViewById(R.id.progressBar);
        tvArtworksEmpty = view.findViewById(R.id.tvArtworksEmpty);
        tvArtistsEmpty = view.findViewById(R.id.tvArtistsEmpty);

        TextView tvSeeAllArtworks = view.findViewById(R.id.tvSeeAllArtworks);
        TextView tvSeeAllArtists = view.findViewById(R.id.tvSeeAllArtists);

        tvSeeAllArtworks.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).bottomNavigationView.setSelectedItemId(R.id.nav_gallery);
            }
        });

        tvSeeAllArtists.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).bottomNavigationView.setSelectedItemId(R.id.nav_artists);
            }
        });
    }

    private void setupRecyclerViews() {
        featuredArtworkAdapter = new ArtworkAdapter(featuredArtworks, artwork -> {
            Intent intent = new Intent(getActivity(), ArtworkDetailActivity.class);
            intent.putExtra("artwork_id", artwork.getId());
            startActivity(intent);
        });

        LinearLayoutManager artworksLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFeaturedArtworks.setLayoutManager(artworksLayoutManager);
        rvFeaturedArtworks.setAdapter(featuredArtworkAdapter);

        featuredArtistsAdapter = new ArtistsAdapter(featuredArtists, artist -> {
            Intent intent = new Intent(getActivity(), ArtistArtworksActivity.class);
            intent.putExtra("artist_id", artist.getId());
            intent.putExtra("artist_name", artist.getUsername());
            startActivity(intent);
        });

        LinearLayoutManager artistsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFeaturedArtists.setLayoutManager(artistsLayoutManager);
        rvFeaturedArtists.setAdapter(featuredArtistsAdapter);
    }

    private void loadFeaturedContent() {
        progressBar.setVisibility(View.VISIBLE);

        artworkViewModel = new ViewModelProvider(requireActivity()).get(ArtworkViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        artworkViewModel.getArtworksResult().observe(getViewLifecycleOwner(), result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    List<Map<String, Object>> artworksData = (List<Map<String, Object>>) result.get("artworks");
                    if (artworksData != null && !artworksData.isEmpty()) {
                        List<Artwork> allArtworks = new ArrayList<>();
                        for (Map<String, Object> artworkData : artworksData) {
                            Artwork artwork = convertToArtwork(artworkData);
                            if (artwork != null) {
                                allArtworks.add(artwork);
                            }
                        }

                        featuredArtworks.clear();
                        if (allArtworks.size() > 4) {
                            Collections.shuffle(allArtworks);
                            featuredArtworks.addAll(allArtworks.subList(0, 4));
                        } else {
                            featuredArtworks.addAll(allArtworks);
                        }
                        featuredArtworkAdapter.notifyDataSetChanged();

                        tvArtworksEmpty.setVisibility(View.GONE);
                        rvFeaturedArtworks.setVisibility(View.VISIBLE);
                        Log.d("HomeFragment", "Loaded " + featuredArtworks.size() + " featured artworks");
                    } else {
                        tvArtworksEmpty.setVisibility(View.VISIBLE);
                        rvFeaturedArtworks.setVisibility(View.GONE);
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(getContext(), "Failed to load artworks: " + message, Toast.LENGTH_SHORT).show();
                    tvArtworksEmpty.setVisibility(View.VISIBLE);
                    rvFeaturedArtworks.setVisibility(View.GONE);
                }
            }
        });

        userViewModel.getArtistsResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Object usersObj = result.get("users");
                    if (usersObj instanceof List) {
                        List<?> usersList = (List<?>) usersObj;
                        List<User> allArtists = new ArrayList<>();

                        for (Object userObj : usersList) {
                            if (userObj instanceof User) {
                                allArtists.add((User) userObj);
                            } else if (userObj instanceof Map) {
                                User user = convertToUser((Map<String, Object>) userObj);
                                if (user != null) {
                                    allArtists.add(user);
                                }
                            }
                        }

                        featuredArtists.clear();
                        if (allArtists.size() > 4) {
                            Collections.shuffle(allArtists);
                            featuredArtists.addAll(allArtists.subList(0, 4));
                        } else {
                            featuredArtists.addAll(allArtists);
                        }
                        featuredArtistsAdapter.notifyDataSetChanged();

                        tvArtistsEmpty.setVisibility(View.GONE);
                        rvFeaturedArtists.setVisibility(View.VISIBLE);
                        Log.d("HomeFragment", "Loaded " + featuredArtists.size() + " featured artists");
                    } else {
                        tvArtistsEmpty.setVisibility(View.VISIBLE);
                        rvFeaturedArtists.setVisibility(View.GONE);
                        Log.d("HomeFragment", "No users data found in response");
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(getContext(), "Failed to load artists: " + message, Toast.LENGTH_SHORT).show();
                    tvArtistsEmpty.setVisibility(View.VISIBLE);
                    rvFeaturedArtists.setVisibility(View.GONE);
                }
            }
        });

        artworkViewModel.getArtworks(0, 50);
        userViewModel.getAllArtists();
    }

    private Artwork convertToArtwork(Map<String, Object> artworkData) {
        try {
            Artwork artwork = new Artwork();

            if (artworkData.get("id") != null) {
                artwork.setId(((Number) artworkData.get("id")).longValue());
            }

            artwork.setTitle((String) artworkData.get("title"));
            artwork.setDescription((String) artworkData.get("description"));
            artwork.setImagePath((String) artworkData.get("imagePath"));
            artwork.setStatus((String) artworkData.get("status"));

            if (artworkData.get("likes") != null) {
                artwork.setLikes(((Number) artworkData.get("likes")).intValue());
            }
            if (artworkData.get("views") != null) {
                artwork.setViews(((Number) artworkData.get("views")).intValue());
            }

            if (artworkData.get("user") != null) {
                Map<String, Object> userData = (Map<String, Object>) artworkData.get("user");
                User user = convertToUser(userData);
                artwork.setUser(user);
            }

            return artwork;
        } catch (Exception e) {
            Log.e("HomeFragment", "Error converting artwork: " + e.getMessage());
            return null;
        }
    }

    private User convertToUser(Map<String, Object> userData) {
        try {
            User user = new User();

            Object idObj = userData.get("id");
            if (idObj != null) {
                if (idObj instanceof Double) {
                    user.setId(((Double) idObj).longValue());
                } else if (idObj instanceof Integer) {
                    user.setId(((Integer) idObj).longValue());
                } else if (idObj instanceof Long) {
                    user.setId((Long) idObj);
                }
            }

            user.setUsername((String) userData.get("username"));
            user.setEmail((String) userData.get("email"));

            Object countObj = userData.get("artworksCount");
            if (countObj != null) {
                if (countObj instanceof Double) {
                    user.setArtworksCount(((Double) countObj).intValue());
                } else if (countObj instanceof Integer) {
                    user.setArtworksCount((Integer) countObj);
                }
            }

            return user;
        } catch (Exception e) {
            Log.e("HomeFragment", "Error converting user: " + e.getMessage());
            return null;
        }
    }
}