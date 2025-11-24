package com.example.vagmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Category;
import com.example.vagmobile.ui.adapter.CategoryAdapter;
import com.example.vagmobile.viewmodel.CategoryViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private CategoryViewModel categoryViewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        initViews();
        setupRecyclerView();
        observeViewModels();
        loadCategories();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryDetailActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(categoryAdapter);
    }

    private void observeViewModels() {
        categoryViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CategoryViewModel.class);

        categoryViewModel.getCategoriesResult().observe(this, result -> {
            progressBar.setVisibility(android.view.View.GONE);

            if (result != null) {
                System.out.println("CategoryActivity: Received result: " + result);
                Boolean success = (Boolean) result.get("success");
                System.out.println("CategoryActivity: Success: " + success);
                
                if (success != null && success) {
                    Object categoriesObj = result.get("categories");
                    System.out.println("CategoryActivity: Categories object: " + categoriesObj);
                    System.out.println("CategoryActivity: Categories object type: " + (categoriesObj != null ? categoriesObj.getClass().getName() : "null"));
                    
                    if (categoriesObj instanceof List) {
                        List<?> categoriesList = (List<?>) categoriesObj;
                        System.out.println("CategoryActivity: Categories list size: " + categoriesList.size());
                        
                        categoryList.clear();
                        for (Object categoryObj : categoriesList) {
                            if (categoryObj instanceof Map) {
                                Map<String, Object> categoryData = (Map<String, Object>) categoryObj;
                                System.out.println("CategoryActivity: Category data: " + categoryData);
                                Category category = convertToCategory(categoryData);
                                categoryList.add(category);
                            }
                        }
                        System.out.println("CategoryActivity: Final category list size: " + categoryList.size());
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        System.out.println("CategoryActivity: Categories is not a List!");
                        Toast.makeText(this, "Failed to parse categories", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String message = (String) result.get("message");
                    System.out.println("CategoryActivity: Error message: " + message);
                    Toast.makeText(this, "Failed to load categories: " + message, Toast.LENGTH_SHORT).show();
                }
            } else {
                System.out.println("CategoryActivity: Result is null!");
                Toast.makeText(this, "No data received", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        categoryViewModel.getCategories();
    }

    private Category convertToCategory(Map<String, Object> categoryData) {
        Category category = new Category();
        
        // Обработка ID
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
        
        category.setName((String) categoryData.get("name"));
        category.setDescription((String) categoryData.get("description"));

        // Обработка approvedArtworksCount
        Object countObj = categoryData.get("approvedArtworksCount");
        if (countObj != null) {
            if (countObj instanceof Double) {
                category.setApprovedArtworksCount(((Double) countObj).longValue());
            } else if (countObj instanceof Integer) {
                category.setApprovedArtworksCount(((Integer) countObj).longValue());
            } else if (countObj instanceof Long) {
                category.setApprovedArtworksCount((Long) countObj);
            }
        }

        return category;
    }
}