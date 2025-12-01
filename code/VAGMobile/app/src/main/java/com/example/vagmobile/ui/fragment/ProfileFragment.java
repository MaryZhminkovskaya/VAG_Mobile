package com.example.vagmobile.ui.fragment;

import android.content.Intent;
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
import com.example.vagmobile.util.SharedPreferencesHelper;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail;
    private Button btnViewProfile, btnLogout, btnAdminCategories, btnAdminArtworks, btnLikedArtworks, btnLogin, btnDocumentation;
    private LinearLayout loggedInLayout, guestLayout;
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
        btnViewProfile = view.findViewById(R.id.btn_view_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnAdminCategories = view.findViewById(R.id.btn_admin_categories);
        btnAdminArtworks = view.findViewById(R.id.btn_admin_artworks);
        btnLikedArtworks = view.findViewById(R.id.btn_liked_artworks);
        btnLogin = view.findViewById(R.id.btn_login);
        btnDocumentation = view.findViewById(R.id.btn_documentation);

        loggedInLayout = view.findViewById(R.id.logged_in_layout);
        guestLayout = view.findViewById(R.id.guest_layout);

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
        // Показываем layout для авторизованных пользователей
        loggedInLayout.setVisibility(View.VISIBLE);
        guestLayout.setVisibility(View.GONE);

        // Загружаем данные пользователя
        loadUserData();
        checkAdminRole();
        setupLoggedInClickListeners();
    }

    private void setupGuestUI() {
        // Показываем layout для гостей
        loggedInLayout.setVisibility(View.GONE);
        guestLayout.setVisibility(View.VISIBLE);

        // Устанавливаем приветствие для гостя
        tvUsername.setText("Гость");
        tvEmail.setText("Войдите в систему для доступа ко всем функциям");

        // Настраиваем кнопку входа
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });
    }

    private void loadUserData() {
        String username = prefs.getUsername();
        String email = prefs.getEmail();

        tvUsername.setText(username != null ? username : "User");
        tvEmail.setText(email != null ? email : "user@example.com");
    }

    private void checkAdminRole() {
        // Проверяем роль пользователя
        String userRole = prefs.getUserRole();
        boolean isAdmin = "ADMIN".equals(userRole);

        btnAdminCategories.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnAdminArtworks.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupLoggedInClickListeners() {
        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        btnLikedArtworks.setOnClickListener(v -> {
            // Открываем активность с понравившимися публикациями
            Intent intent = new Intent(getActivity(), LikedArtworksActivity.class);
            startActivity(intent);
        });

        btnDocumentation.setOnClickListener(v -> {
            // Открываем фрагмент документации
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
            // Обновляем UI после выхода
            setupUI();
            Toast.makeText(getContext(), "Вы вышли из системы", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем UI при возвращении на фрагмент
        setupUI();
    }
}