package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final double LOW_BUDGET_THRESHOLD = 0.2; // 20% of budget
    private Animation pulseAnimation;
    private boolean isAnimating = false;
    private ProgressBar budgetProgBar;
    private FirebaseFirestore db;
    private TextView expenseInputTextView, dateTextView;
    private FirebaseManager firebaseManager;
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private Button addExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);

        firebaseManager = new FirebaseManager(this);

        budgetProgBar = findViewById(R.id.progress_circular);
        expenseInputTextView = findViewById(R.id.expenseinput);
        dateTextView = findViewById(R.id.dateTextView);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        addExpenseButton = findViewById(R.id.addExpense);

        setupRecyclerView();

        db = FirebaseFirestore.getInstance();

        addExpenseButton.setOnClickListener(v -> showCategoryDialog());

        firebaseManager.signInAnonymouslyIfNeeded(new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d("AuthCheck", "Signed in as: " + user.getUid());
                loadBudgetData();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AuthCheck", "Authentication failed: " + e.getMessage());
            }
        });

        budgetProgBar.setOnClickListener(v -> {
            ExpenseTarget expenseTarget = new ExpenseTarget();
            expenseTarget.show(getSupportFragmentManager(), "dialogbox_expensetarget");
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.menu) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
                return true; // Indicate that the item selection was handled
            } else if (itemId == R.id.profile) {
                // Navigate to Profile page
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true; // Indicate that the item selection was handled
            }
            return false; // Indicate that the item selection was not handled
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadBudgetData();
        loadExpenses(); // Load expenses when activity starts
    }

    private void loadBudgetData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            db.collection("users")
                    .document(uid)
                    .collection("expenses")
                    .document("expenseTarget")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String budget = documentSnapshot.getString("budget");
                            String startDate = documentSnapshot.getString("startDate");
                            String endDate = documentSnapshot.getString("endDate");

                            if (budget != null) {
                                updateBudgetText(budget);
                                updateMaxBudget(Integer.parseInt(budget));
                                updateDateText(startDate, endDate);
                            } else {
                                Log.d("Firestore", "No budget data available.");
                            }
                        } else {
                            Log.d("Firestore", "No such document");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firestore", "Error getting document: " + e.getMessage());
                    });
        }
    }

    public void updateDateText(String start, String end) {
        dateTextView.setText(start + " - " + end);
    }

    public void updateBudgetText(String budget) {
        expenseInputTextView.setText("Php " + budget);
    }

    public void updateMaxBudget(int budgetMax) {
        budgetProgBar.setMax(budgetMax);
        budgetProgBar.setProgress(budgetMax); // Start from maximum (full)
        expenseInputTextView.setText(String.format("Php %d", budgetMax));
    }

    private void showCategoryDialog() {
        final Dialog categoryDialog = new Dialog(this);
        categoryDialog.setContentView(R.layout.dialogbox_category);
        categoryDialog.setCancelable(true);

        Button btnSavings = categoryDialog.findViewById(R.id.btnSavings);
        Button btnFood = categoryDialog.findViewById(R.id.btnFood);
        Button btnTranspo = categoryDialog.findViewById(R.id.btnTranspo);
        Button btnUtilities = categoryDialog.findViewById(R.id.btnUtilities);
        Button btnGrocery = categoryDialog.findViewById(R.id.btnGrocery);
        Button btnShopping = categoryDialog.findViewById(R.id.btnShopping);
        Button btnOthers = categoryDialog.findViewById(R.id.btnOthers);

        View.OnClickListener categoryClickListener = v -> {
            String category = ((Button) v).getText().toString();
            categoryDialog.dismiss();
            if (category.equals("Savings")) {
                showSavingsDialog(category);
            } else {
                showExpenseDialog(category);
            }
        };

        btnSavings.setOnClickListener(categoryClickListener);
        btnFood.setOnClickListener(categoryClickListener);
        btnTranspo.setOnClickListener(categoryClickListener);
        btnUtilities.setOnClickListener(categoryClickListener);
        btnGrocery.setOnClickListener(categoryClickListener);
        btnShopping.setOnClickListener(categoryClickListener);
        btnOthers.setOnClickListener(categoryClickListener);

        categoryDialog.show();
    }

    private void showExpenseDialog(String category) {
        final Dialog expenseDialog = new Dialog(this);
        expenseDialog.setContentView(R.layout.dialogbox_expenses_main);
        expenseDialog.setCancelable(true);

        EditText etAmount = expenseDialog.findViewById(R.id.etAmount);
        EditText etDescription = expenseDialog.findViewById(R.id.etDescription);
        Button btnSave = expenseDialog.findViewById(R.id.btnSave);
        Button btnCancel = expenseDialog.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            String description = etDescription.getText().toString();
            if (!amount.isEmpty()) {
                saveExpense(category, Double.parseDouble(amount), description, false);
                expenseDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> expenseDialog.dismiss());

        expenseDialog.show();
    }

    private void showSavingsDialog(String category) {
        final Dialog savingsDialog = new Dialog(this);
        savingsDialog.setContentView(R.layout.dialogbox_savings_main);
        savingsDialog.setCancelable(true);

        EditText etAmount = savingsDialog.findViewById(R.id.etAmount);
        EditText etDescription = savingsDialog.findViewById(R.id.etDescription);
        Button btnSave = savingsDialog.findViewById(R.id.btnSave);
        Button btnCancel = savingsDialog.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            String description = etDescription.getText().toString();
            if (!amount.isEmpty()) {
                saveExpense(category, Double.parseDouble(amount), description, true);
                savingsDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> savingsDialog.dismiss());

        savingsDialog.show();
    }

    private void saveExpense(String category, double amount, String description, boolean isSavings) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        ExpenseItem expense = new ExpenseItem(category, amount, description, currentDate, isSavings);

        // Save to Firebase
        firebaseManager.saveExpense(expense, new FirebaseManager.ExpenseDataCallback() {
            @Override
            public void onExpensesLoaded(List<ExpenseItem> expenses) {
                // Update the adapter with all expenses
                expenseAdapter.clearExpenses();
                for (ExpenseItem exp : expenses) {
                    expenseAdapter.addExpense(exp);
                }

                // Update progress bar
                updateProgressBar(expenses);
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Error saving expense: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Failed to save expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExpenses() {
        firebaseManager.loadExpenses(new FirebaseManager.ExpenseDataCallback() {
            @Override
            public void onExpensesLoaded(List<ExpenseItem> expenses) {
                // Update the adapter with loaded expenses
                expenseAdapter.clearExpenses();
                for (ExpenseItem expense : expenses) {
                    expenseAdapter.addExpense(expense);
                }

                // Get the current budget target
                int budgetMax = budgetProgBar.getMax();

                // If there are no expenses, set progress bar to max (full budget)
                if (expenses.isEmpty()) {
                    // Set progress immediately to max
                    budgetProgBar.setProgress(budgetMax);

                    // Animate to max for visual feedback
                    ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                            budgetProgBar,
                            "progress",
                            0,
                            budgetMax
                    );
                    progressAnimator.setDuration(1000);
                    progressAnimator.setInterpolator(new DecelerateInterpolator());
                    progressAnimator.start();

                    // Update the budget text to show full amount
                    expenseInputTextView.setText(String.format("Php %d", budgetMax));

                    // Make sure we're using the normal circle drawable
                    budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle));
                    budgetProgBar.clearAnimation();
                    isAnimating = false;
                } else {
                    // Update progress bar with expenses
                    updateProgressBar(expenses);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Error loading expenses: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Failed to load expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProgressBar(List<ExpenseItem> expenses) {
        // Get the current budget target
        int budgetMax = budgetProgBar.getMax();
        int currentProgress = budgetProgBar.getProgress();

        // If there are no expenses, animate to max budget
        if (expenses == null || expenses.isEmpty()) {
            // Set normal circle drawable and clear any existing animations
            budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle));
            budgetProgBar.clearAnimation();
            isAnimating = false;

            // Animate to max with smooth interpolation
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                    budgetProgBar,
                    "progress",
                    currentProgress,
                    budgetMax
            );
            progressAnimator.setDuration(1500); // Longer duration for smoother effect
            progressAnimator.setInterpolator(new DecelerateInterpolator());
            progressAnimator.start();

            // Update the budget text to show full amount
            expenseInputTextView.setText(String.format("Php %d", budgetMax));
            return;
        }

        double totalExpenses = 0;
        double totalSavings = 0;

        // Calculate total expenses and savings
        for (ExpenseItem expense : expenses) {
            if (expense.isSavings()) {
                totalSavings += expense.getAmount();
            } else {
                totalExpenses += expense.getAmount();
            }
        }

        // Calculate remaining budget
        int remainingBudget = budgetMax - (int) (totalExpenses + totalSavings);
        remainingBudget = Math.max(0, remainingBudget);

        // Calculate budget percentage
        double budgetPercentage = (double) remainingBudget / budgetMax;

        // Animate to the new remaining budget value
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(
                budgetProgBar,
                "progress",
                currentProgress,
                remainingBudget
        );
        progressAnimator.setDuration(1500); // Longer duration for smoother effect
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();

        // Update the budget text to show remaining amount
        expenseInputTextView.setText(String.format("Php %d", remainingBudget));

        // Check if budget is low
        if (budgetPercentage <= LOW_BUDGET_THRESHOLD) {
            budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle_red));
            if (!isAnimating) {
                budgetProgBar.startAnimation(pulseAnimation);
                isAnimating = true;
            }
            showLowBudgetWarning(remainingBudget, budgetMax);
        } else {
            budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle));
            budgetProgBar.clearAnimation();
            isAnimating = false;
        }
    }

    private void showLowBudgetWarning(int remainingBudget, int totalBudget) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_low_budget_warning);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView warningMessage = dialog.findViewById(R.id.warningMessage);
        double percentage = (remainingBudget * 100.0) / totalBudget;
        warningMessage.setText(String.format("Your budget is running low! You have %.1f%% (Php %d) remaining.",
                percentage, remainingBudget));

        Button btnOk = dialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteConfirmationDialog(ExpenseItem expense, int position) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirmDelete = dialog.findViewById(R.id.btnConfirmDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            firebaseManager.deleteExpense(expense, new FirebaseManager.DeleteExpenseCallback() {
                @Override
                public void onExpenseDeleted() {
                    runOnUiThread(() -> {
                        // Remove from adapter and update UI immediately
                        expenseAdapter.removeExpense(position);

                        // Get current expenses after deletion
                        List<ExpenseItem> currentExpenses = expenseAdapter.getExpenseList();

                        // Update progress bar with remaining expenses
                        updateProgressBar(currentExpenses);

                        Toast.makeText(MainActivity.this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error deleting expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }
            });
        });

        dialog.show();
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), (expense, position) -> showDeleteConfirmationDialog(expense, position));
        expenseRecyclerView.setAdapter(expenseAdapter);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
