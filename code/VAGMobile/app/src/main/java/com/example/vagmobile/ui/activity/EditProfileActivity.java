package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vagmobile.R;
import com.example.vagmobile.util.SharedPreferencesHelper;
import com.example.vagmobile.viewmodel.UserViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etDescription;
    private Button btnSave, btnCancel;
    private SharedPreferencesHelper prefs;
    private UserViewModel userViewModel;

    // Используем правильный SharedPreferences
    private SharedPreferences profilePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = new SharedPreferencesHelper(this);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // ИСПРАВЛЕНО: Используем общие SharedPreferences
        profilePrefs = getSharedPreferences("user_profile", MODE_PRIVATE);

        initViews();
        loadCurrentProfile();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadCurrentProfile() {
        String username = prefs.getUsername();
        String email = prefs.getEmail();
        String description = getDescriptionFromPrefs();

        etUsername.setText(username != null ? username : "");
        etEmail.setText(email != null ? email : "");
        etDescription.setText(description != null ? description : "");
    }

    private String getDescriptionFromPrefs() {
        // ИСПРАВЛЕНО: Используем общие SharedPreferences
        return profilePrefs.getString("user_description", "");
    }

    private void saveDescriptionToPrefs(String description) {
        // ИСПРАВЛЕНО: Используем общие SharedPreferences
        SharedPreferences.Editor editor = profilePrefs.edit();
        editor.putString("user_description", description);
        editor.apply();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Введите имя пользователя");
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Введите email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректный email");
            etEmail.requestFocus();
            return;
        }

        // Сохраняем локально
        prefs.setUsername(username);
        prefs.setEmail(email);
        saveDescriptionToPrefs(description);

        Toast.makeText(this, "Профиль сохранен", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void observeViewModel() {
        // TODO: Добавить observer для обновления профиля через API
    }
}