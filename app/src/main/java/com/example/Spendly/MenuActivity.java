package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//Kyle Start Code
public class MenuActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();
    private FirebaseManager firebaseManager;
    private TextView totalExpensesTextView;
    private Map<String, Double> categoryTotals = new HashMap<>();
    private ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        rvCategories = findViewById(R.id.rvCategories);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);

        // Set up categories RecyclerView
        categoryAdapter = new CategoryAdapter(categories, (category, position) -> {
            showDeleteConfirmationDialog(category, position);
        });
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        // Set up expenses RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), (expense, position) -> showDeleteExpenseConfirmationDialog(expense, position));
        recyclerView.setAdapter(expenseAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseManager = new FirebaseManager(this);

        // Using the sign-in method from FirebaseManager
        firebaseManager.signInAnonymouslyIfNeeded(new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Load categories once signed in
                loadExpenses();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MenuActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MenuActivity", "Anonymous sign-in failed", e);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.menu) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.home) {
                // Navigate to MainActivity2 (Menu page)
                intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true; // Indicate that the item selection was handled
            } else if (itemId == R.id.profile) {
                // Navigate to Profile page
                intent = new Intent(MenuActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true; // Indicate that the item selection was handled
            }
            if (intent != null) {
                // Clear the back stack and start a new task
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadExpenses() {
        firebaseManager.loadExpenses(new FirebaseManager.ExpenseDataCallback() {
            @Override
            public void onExpensesLoaded(List<ExpenseItem> expenses) {
                categoryTotals.clear();
                categories.clear();
                Map<String, List<ExpenseItem>> categoryExpenses = new HashMap<>();

                // Group expenses by category
                for (ExpenseItem expense : expenses) {
                    String category = expense.getCategory();
                    double amount = expense.getAmount();

                    // Add to category totals
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);

                    // Group expenses by category
                    if (!categoryExpenses.containsKey(category)) {
                        categoryExpenses.put(category, new ArrayList<>());
                    }
                    categoryExpenses.get(category).add(expense);
                }

                // Create category objects with their expenses
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    String categoryName = entry.getKey();
                    Category category = new Category(categoryName, entry.getValue());
                    category.setExpenses(categoryExpenses.getOrDefault(categoryName, new ArrayList<>()));
                    categories.add(category);
                }

                // Calculate total expenses
                double totalExpenses = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                totalExpensesTextView.setText(String.format("Total Expenses: Php %.2f", totalExpenses));

                // Update both adapters
                categoryAdapter.notifyDataSetChanged();
                expenseAdapter.clearExpenses();
                for (ExpenseItem expense : expenses) {
                    expenseAdapter.addExpense(expense);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MenuActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                Log.e("MenuActivity", "Error loading expenses", e);
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
                    loadExpenses();
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

    private void showDeleteConfirmationDialog(Category category, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firebaseManager.deleteCategory(category, new FirebaseManager.DeleteCategoryCallback() {
                        @Override
                        public void onCategoryDeleted() {
                            runOnUiThread(() -> {
                                loadExpenses(); // Reload all expenses after deletion
                                Toast.makeText(MenuActivity.this, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MenuActivity.this, "Error deleting category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteExpenseConfirmationDialog(ExpenseItem expense, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firebaseManager.deleteExpense(expense, new FirebaseManager.DeleteExpenseCallback() {
                        @Override
                        public void onExpenseDeleted() {
                            runOnUiThread(() -> {
                                Toast.makeText(MenuActivity.this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                                loadExpenses(); // Reload all expenses to update the UI
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MenuActivity.this, "Error deleting expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(); // Reload expenses when activity resumes
    }

}
//Kyle End Code