package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();
    private FloatingActionButton fabAddCategory;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        rvCategories = findViewById(R.id.rvCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        categoryAdapter = new CategoryAdapter(categories);
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        firebaseManager = new FirebaseManager(this);

        // Using the sign-in method from FirebaseManager
        firebaseManager.signInAnonymouslyIfNeeded(new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Load categories once signed in
                loadCategories();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MenuActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MenuActivity", "Anonymous sign-in failed", e);
            }
        });

        // Floating Action Button to add a new category
        fabAddCategory.setOnClickListener(view -> showAddCategoryDialog());


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if (itemId == R.id.menu) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.home) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true; // Indicate that the item selection was handled
            }else if (itemId == R.id.profile) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                return true; // Indicate that the item selection was handled
            }
            return false; // Indicate that the item selection was not handled
        });

    }

    private void loadCategories() {
        firebaseManager.loadCategories(new FirebaseManager.CategoryDataCallback() {
            @Override
            public void onCategoriesLoaded(List<Category> loadedCategories) {
                categories.clear();
                categories.addAll(loadedCategories);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MenuActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                Log.e("MenuActivity", "Error loading categories", e);
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        EditText etLabel = dialogView.findViewById(R.id.etLabel);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        AlertDialog dialog = builder.create();

        btnAddCategory.setOnClickListener(view -> {
            String categoryName = etCategoryName.getText().toString();
            String label = etLabel.getText().toString();

            // Create a new Category object
            Category newCategory = new Category(categoryName, label);

            // Save the category to Firestore
            firebaseManager.saveCategory(newCategory, new FirebaseManager.CategoryDataCallback() {
                @Override
                public void onCategoriesLoaded(List<Category> categories) {
                    // After saving, reload the categories
                    loadCategories();
                    dialog.dismiss();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(MenuActivity.this, "Failed to add category", Toast.LENGTH_SHORT).show();
                    Log.e("MenuActivity", "Error adding category", e);
                }
            });
        });

        dialog.show();
    }
}
