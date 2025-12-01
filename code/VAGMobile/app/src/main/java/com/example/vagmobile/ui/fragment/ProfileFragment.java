package com.example.vagmobile.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.vagmobile.R;
import com.example.vagmobile.ui.activity.LoginActivity;
import com.example.vagmobile.ui.activity.ProfileActivity;
import com.example.vagmobile.ui.activity.AdminCategoriesActivity;
import com.example.vagmobile.ui.activity.AdminArtworksActivity;
import com.example.vagmobile.ui.activity.LikedArtworksActivity;
import com.example.vagmobile.ui.activity.UserArtworksActivity;
import com.example.vagmobile.ui.activity.EditProfileActivity;
import com.example.vagmobile.util.SharedPreferencesHelper;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvDescription;
    private Button btnViewProfile, btnLogout, btnAdminCategories, btnAdminArtworks,
            btnLikedArtworks, btnLogin, btnDocumentation, btnMyArtworks, btnEditProfile;
    private LinearLayout loggedInLayout, guestLayout, adminSection;
    private SharedPreferencesHelper prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupUI();

        return view;
    }

    private void initViews(View view) {
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvDescription = view.findViewById(R.id.tv_description);
        btnViewProfile = view.findViewById(R.id.btn_view_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnAdminCategories = view.findViewById(R.id.btn_admin_categories);
        btnAdminArtworks = view.findViewById(R.id.btn_admin_artworks);
        btnLikedArtworks = view.findViewById(R.id.btn_liked_artworks);
        btnLogin = view.findViewById(R.id.btn_login);
        btnDocumentation = view.findViewById(R.id.btn_documentation);
        btnMyArtworks = view.findViewById(R.id.btn_my_artworks);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        loggedInLayout = view.findViewById(R.id.logged_in_layout);
        guestLayout = view.findViewById(R.id.guest_layout);
        adminSection = view.findViewById(R.id.admin_section);

        prefs = new SharedPreferencesHelper(getContext());
    }

    private void setupUI() {
        if (prefs.isLoggedIn()) {
            setupLoggedInUI();
        } else {
            setupGuestUI();
        }
    }

    private void setupLoggedInUI() {
        loggedInLayout.setVisibility(View.VISIBLE);
        guestLayout.setVisibility(View.GONE);

        loadUserData();
        checkAdminRole();
        setupLoggedInClickListeners();
    }

    private void setupGuestUI() {
        loggedInLayout.setVisibility(View.GONE);
        guestLayout.setVisibility(View.VISIBLE);

        tvUsername.setText("Гость");
        tvEmail.setText("Войдите в систему для доступа ко всем функциям");
        tvDescription.setText("");

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });
    }

    private void loadUserData() {
        String username = prefs.getUsername();
        String email = prefs.getEmail();
        String description = getDescriptionFromPrefs();

        tvUsername.setText(username != null ? username : "Пользователь");
        tvEmail.setText(email != null ? email : "user@example.com");

        // ИСПРАВЛЕНО: Если нет описания, показываем текст
        if (description != null && !description.trim().isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("Описание не добавлено");
            tvDescription.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private String getDescriptionFromPrefs() {
        // ИСПРАВЛЕНО: Используем те же SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("user_profile", Context.MODE_PRIVATE);
        return sharedPref.getString("user_description", "");
    }

    private void saveDescriptionToPrefs(String description) {
        // ИСПРАВЛЕНО: Используем те же SharedPreferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("user_profile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_description", description);
        editor.apply();
    }

    private void checkAdminRole() {
        String userRole = prefs.getUserRole();
        boolean isAdmin = "ADMIN".equals(userRole) || "ROLE_ADMIN".equals(userRole);

        if (isAdmin) {
            btnAdminCategories.setVisibility(View.VISIBLE);
            btnAdminArtworks.setVisibility(View.VISIBLE);
            adminSection.setVisibility(View.VISIBLE);
        } else {
            btnAdminCategories.setVisibility(View.GONE);
            btnAdminArtworks.setVisibility(View.GONE);
            adminSection.setVisibility(View.GONE);
        }
    }

    private void setupLoggedInClickListeners() {
        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnMyArtworks.setOnClickListener(v -> {
            Long userId = prefs.getUserId();
            if (userId != null) {
                Intent intent = new Intent(getActivity(), UserArtworksActivity.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("is_own_profile", true);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
            }
        });

        btnLikedArtworks.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LikedArtworksActivity.class);
            startActivity(intent);
        });

        btnDocumentation.setOnClickListener(v -> {
            DocumentationFragment documentationFragment = new DocumentationFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, documentationFragment)
                    .addToBackStack("profile")
                    .commit();
        });

        btnAdminCategories.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdminCategoriesActivity.class);
            startActivity(intent);
        });

        btnAdminArtworks.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdminArtworksActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            prefs.clearUserData();
            setupUI();
            Toast.makeText(getContext(), "Вы вышли из системы", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUI();
    }
}