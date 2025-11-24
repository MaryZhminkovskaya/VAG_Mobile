package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.vagmobile.R;
import com.example.vagmobile.util.SharedPreferencesHelper;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvUserId = findViewById(R.id.tvUserId);
    }

    private void loadUserData() {
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(this);
        Long userId = prefs.getUserId();
        String username = prefs.getUsername();
        String email = prefs.getEmail();

        tvUsername.setText(username != null ? username : "Unknown");
        tvEmail.setText(email != null ? email : "Unknown");
        tvUserId.setText(userId != null ? "User ID: " + userId : "User ID: Unknown");
    }
}