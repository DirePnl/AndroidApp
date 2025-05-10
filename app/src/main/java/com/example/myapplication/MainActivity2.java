package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu); // Set menu as selected initially

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                // Navigate to MainActivity (Home page)
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                // You might want to finish the current activity if you don't want to go back to it
                // finish();
                return true; // Indicate that the item selection was handled
            } else if (itemId == R.id.menu) {
                // Already on the menu page, no action needed
                return true;
            }
            return false; // Indicate that the item selection was not handled
        });




        rvCategories = findViewById(R.id.rvCategories);
        categoryAdapter = new CategoryAdapter(categories);
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        fabAddCategory = findViewById(R.id.fabAddCategory);
        fabAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCategoryDialog();
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

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryName = etCategoryName.getText().toString();
                String label = etLabel.getText().toString();

                Category newCategory = new Category(categoryName, label);

                categoryAdapter.addCategory(newCategory);

                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

