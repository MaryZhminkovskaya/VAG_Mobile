package com.example.vagmobile.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.vagmobile.R;
import com.example.vagmobile.ui.fragment.HomeFragment;
import com.example.vagmobile.ui.fragment.MoreFragment;
import com.example.vagmobile.ui.fragment.ProfileFragment;
import com.example.vagmobile.util.SharedPreferencesHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    public BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();
        setupFloatingActionButton();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabCreate = findViewById(R.id.fab_create);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_more) {
                    selectedFragment = new MoreFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }

                return true;
            }
        });
    }

    private void setupFloatingActionButton() {
        fabCreate.setOnClickListener(v -> {
            SharedPreferencesHelper prefs = new SharedPreferencesHelper(MainActivity.this);
            if (!prefs.isLoggedIn()) {
                Toast.makeText(MainActivity.this, "Пожалуйста, войдите в систему чтобы создавать контент", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return;
            }

            showCreateContentDialog();
        });
    }

    private void showCreateContentDialog() {
        String[] options = {"Создать публикацию", "Создать выставку"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите тип контента")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Создать публикацию
                            Intent artworkIntent = new Intent(MainActivity.this, CreateArtworkActivity.class);
                            startActivity(artworkIntent);
                            break;
                        case 1: // Создать выставку
                            Intent exhibitionIntent = new Intent(MainActivity.this, CreateExhibitionActivity.class);
                            startActivity(exhibitionIntent);
                            break;
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (bottomNavigationView.getSelectedItemId() == R.id.nav_home) {
                super.onBackPressed();
                finishAffinity();
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }
}