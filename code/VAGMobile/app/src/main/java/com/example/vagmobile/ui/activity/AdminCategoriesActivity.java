package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.vagmobile.R;
import com.example.vagmobile.model.Category;
import com.example.vagmobile.ui.adapter.AdminCategoryAdapter;
import com.example.vagmobile.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminCategoriesActivity extends AppCompatActivity {

    private CategoryViewModel categoryViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btnAddCategory;
    private AdminCategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_categories);

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadCategories();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void setupRecyclerView() {
        categoryAdapter = new AdminCategoryAdapter(categoryList, new AdminCategoryAdapter.CategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                showDeleteConfirmationDialog(category);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);
    }

    private void observeViewModels() {
        categoryViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CategoryViewModel.class);

        // Наблюдатель для загрузки категорий
        categoryViewModel.getCategoriesResult().observe(this, result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    List<Map<String, Object>> categoriesData = (List<Map<String, Object>>) result.get("categories");
                    if (categoriesData != null) {
                        categoryList.clear();
                        for (Map<String, Object> categoryData : categoriesData) {
                            Category category = convertToCategory(categoryData);
                            categoryList.add(category);
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to load categories: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Наблюдатель для создания категории
        categoryViewModel.getCreateCategoryResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Toast.makeText(this, "Category created successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Перезагружаем список
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to create category: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Наблюдатель для обновления категории
        categoryViewModel.getUpdateCategoryResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Перезагружаем список
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to update category: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Наблюдатель для удаления категории
        categoryViewModel.getDeleteCategoryResult().observe(this, result -> {
            if (result != null) {
                Boolean success = (Boolean) result.get("success");
                if (success != null && success) {
                    Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Перезагружаем список
                } else {
                    String message = (String) result.get("message");
                    Toast.makeText(this, "Failed to delete category: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        categoryViewModel.getCategories();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        builder.setView(dialogView)
                .setTitle("Add Category")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createCategory(name, description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        etName.setText(category.getName());
        etDescription.setText(category.getDescription());

        builder.setView(dialogView)
                .setTitle("Edit Category")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateCategory(category.getId(), name, description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteCategory(category.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createCategory(String name, String description) {
        categoryViewModel.createCategory(name, description);
    }

    private void updateCategory(Long categoryId, String name, String description) {
        categoryViewModel.updateCategory(categoryId, name, description);
    }

    private void deleteCategory(Long categoryId) {
        categoryViewModel.deleteCategory(categoryId);
    }

    private Category convertToCategory(Map<String, Object> categoryData) {
        Category category = new Category();

        Object idObj = categoryData.get("id");
        if (idObj != null) {
            if (idObj instanceof Double) {
                category.setId(((Double) idObj).longValue());
            } else if (idObj instanceof Integer) {
                category.setId(((Integer) idObj).longValue());
            } else if (idObj instanceof Long) {
                category.setId((Long) idObj);
            }
        }

        category.setName(categoryData.get("name") != null ? categoryData.get("name").toString() : "Без названия");
        category.setDescription(categoryData.get("description") != null ? categoryData.get("description").toString() : "");

        Object countObj = categoryData.get("approvedArtworksCount");
        if (countObj != null) {
            if (countObj instanceof Double) {
                category.setApprovedArtworksCount(((Double) countObj).longValue());
            } else if (countObj instanceof Integer) {
                category.setApprovedArtworksCount(((Integer) countObj).longValue());
            } else if (countObj instanceof Long) {
                category.setApprovedArtworksCount((Long) countObj);
            }
        } else {
            category.setApprovedArtworksCount(0L);
        }

        return category;
    }
}