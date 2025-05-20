package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.List;

public class AddCategory extends AppCompatActivity {

    private EditText etCategoryName, etLabel;
    private Button btnAddCategory;
    private FirebaseManager firebaseManager = new FirebaseManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_category);

        etCategoryName = findViewById(R.id.etCategoryName);
        etLabel = findViewById(R.id.etLabel);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        btnAddCategory.setOnClickListener(view -> {
            String categoryName = etCategoryName.getText().toString().trim();
            String label = etLabel.getText().toString().trim();

            if (!categoryName.isEmpty() && !label.isEmpty()) {
                // Create new category object
                Category newCategory = new Category(categoryName, label);

                // Save to Firebase with a callback
                firebaseManager.saveCategory(newCategory, new FirebaseManager.CategoryDataCallback() {
                    @Override
                    public void onCategoriesLoaded(List<Category> categories) {
                        // You could handle success here if needed (e.g., refresh UI, show a message, etc.)
                        Toast.makeText(AddCategory.this, "Category added!", Toast.LENGTH_SHORT).show();

                        // Return to MenuActivity and reload categories
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish(); // Close AddCategory activity and go back
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle the error if saving failed
                        Toast.makeText(AddCategory.this, "Failed to add category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // Clear the fields
                etCategoryName.setText("");
                etLabel.setText("");
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
