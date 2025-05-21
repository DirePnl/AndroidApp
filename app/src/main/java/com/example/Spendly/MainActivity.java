package com.example.Spendly;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private Dialog currentLowBudgetDialog = null;
    private Dialog currentDateWarningDialog = null;
    private Dialog currentCategoryDialog = null;
    private Dialog currentExpenseDialog = null;
    private Dialog currentSavingsDialog = null;
    private Dialog currentDeleteDialog = null;

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
            Intent intent = null;
            if (itemId == R.id.home) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.menu) {
                // Navigate to Menu page
                intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true; // Indicate that the item selection was handled
            } else if (itemId == R.id.profile) {
                // Navigate to Profile page
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true; // Indicate that the item selection was handled
            }
            if (intent != null) {
                startActivity(intent);
                finish(); // Prevent stacking
                return true;
            }
            return false; // Indicate that the item selection was not handled
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
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

                                // Check if end date is within 5 days
                                checkEndDateWarning(endDate);

                                // Clear and reload expenses when budget data is loaded
                                expenseAdapter.clearExpenses();
                                loadExpenses();
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
        // Dismiss any existing dialog
        dismissAllDialogs();

        currentCategoryDialog = new Dialog(this);
        currentCategoryDialog.setContentView(R.layout.dialogbox_category);
        currentCategoryDialog.setCancelable(true);

        Button btnSavings = currentCategoryDialog.findViewById(R.id.btnSavings);
        Button btnFood = currentCategoryDialog.findViewById(R.id.btnFood);
        Button btnTranspo = currentCategoryDialog.findViewById(R.id.btnTranspo);
        Button btnUtilities = currentCategoryDialog.findViewById(R.id.btnUtilities);
        Button btnGrocery = currentCategoryDialog.findViewById(R.id.btnGrocery);
        Button btnShopping = currentCategoryDialog.findViewById(R.id.btnShopping);
        Button btnOthers = currentCategoryDialog.findViewById(R.id.btnOthers);

        View.OnClickListener categoryClickListener = v -> {
            String category = ((Button) v).getText().toString();
            currentCategoryDialog.dismiss();
            currentCategoryDialog = null;
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

        currentCategoryDialog.show();
    }

    private void showExpenseDialog(String category) {
        // Dismiss any existing dialog
        dismissAllDialogs();

        currentExpenseDialog = new Dialog(this);
        currentExpenseDialog.setContentView(R.layout.dialogbox_expenses_main);
        currentExpenseDialog.setCancelable(true);

        EditText etAmount = currentExpenseDialog.findViewById(R.id.etAmount);
        EditText etDescription = currentExpenseDialog.findViewById(R.id.etDescription);
        Button btnSave = currentExpenseDialog.findViewById(R.id.btnSave);
        Button btnCancel = currentExpenseDialog.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            String description = etDescription.getText().toString();
            if (!amount.isEmpty()) {
                saveExpense(category, Double.parseDouble(amount), description, false);
                currentExpenseDialog.dismiss();
                currentExpenseDialog = null;
            }
        });

        btnCancel.setOnClickListener(v -> {
            currentExpenseDialog.dismiss();
            currentExpenseDialog = null;
        });

        currentExpenseDialog.show();
    }

    private void showSavingsDialog(String category) {
        // Dismiss any existing dialog
        dismissAllDialogs();

        currentSavingsDialog = new Dialog(this);
        currentSavingsDialog.setContentView(R.layout.dialogbox_savings_main);
        currentSavingsDialog.setCancelable(true);

        EditText etAmount = currentSavingsDialog.findViewById(R.id.etAmount);
        EditText etDescription = currentSavingsDialog.findViewById(R.id.etDescription);
        Button btnSave = currentSavingsDialog.findViewById(R.id.btnSave);
        Button btnCancel = currentSavingsDialog.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            String description = etDescription.getText().toString();
            if (!amount.isEmpty()) {
                saveExpense(category, Double.parseDouble(amount), description, true);
                currentSavingsDialog.dismiss();
                currentSavingsDialog = null;
            }
        });

        btnCancel.setOnClickListener(v -> {
            currentSavingsDialog.dismiss();
            currentSavingsDialog = null;
        });

        currentSavingsDialog.show();
    }

    private void refreshActivity() {
        // 1. Clear any dialogs
        dismissAllDialogs();

        // 2. Clear adapter data
        expenseAdapter.clearExpenses();

        // 3. Reload new data (this should call setExpenses() or add new data to adapter)
        loadBudgetData();
        loadExpenses(); // this must update the adapter and call notifyDataSetChanged()

        // 4. Notify adapter (only if loadExpenses doesn't already do it)
        expenseAdapter.notifyDataSetChanged();

        // 5. Optional UI tweaks
        expenseRecyclerView.invalidate();
        expenseRecyclerView.requestLayout();
        expenseRecyclerView.smoothScrollToPosition(0);
    }



    @Override
    protected void onResume() {
        super.onResume();
        refreshActivity();
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

                // Refresh the activity
                refreshActivity();
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

                // Notify adapter of data change
                expenseAdapter.notifyDataSetChanged();

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

    private void checkEndDateWarning(String endDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date endDate = sdf.parse(endDateStr);
            Date currentDate = new Date();

            // Calculate days difference
            long diffInMillis = endDate.getTime() - currentDate.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            if (diffInDays <= 5 && diffInDays >= 0) {
                showDateWarningDialog(diffInDays);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing date: " + e.getMessage());
        }
    }

    private void showDateWarningDialog(long daysRemaining) {
        // Dismiss any existing dialog
        dismissAllDialogs();

        currentDateWarningDialog = new Dialog(this);
        currentDateWarningDialog.setContentView(R.layout.dialog_date_warning);
        currentDateWarningDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView warningMessage = currentDateWarningDialog.findViewById(R.id.warningMessage);
        warningMessage.setText(String.format("Your budget period will end in %d days. Consider setting a new budget target.", daysRemaining));

        Button btnOk = currentDateWarningDialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            currentDateWarningDialog.dismiss();
            currentDateWarningDialog = null;
        });

        currentDateWarningDialog.show();
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

        // Calculate remaining budget (can be negative)
        int remainingBudget = budgetMax - (int) (totalExpenses + totalSavings);

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

        // Check if budget is low or negative
        if (remainingBudget <= 0) {
            // For negative budget, use red color
            budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle_red));
            budgetProgBar.clearAnimation();
            isAnimating = false;
            showLowBudgetWarning(remainingBudget, budgetMax);
        } else {
            double budgetPercentage = (double) remainingBudget / budgetMax;
            if (budgetPercentage <= LOW_BUDGET_THRESHOLD) {
                budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle_red));
                if (!isAnimating) {
                    budgetProgBar.startAnimation(pulseAnimation);
                    isAnimating = true;
                }
                // Only show warning if we haven't shown it for negative budget
                if (remainingBudget > 0) {
                    showLowBudgetWarning(remainingBudget, budgetMax);
                }
            } else {
                budgetProgBar.setProgressDrawable(getResources().getDrawable(R.drawable.circle));
                budgetProgBar.clearAnimation();
                isAnimating = false;
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void showLowBudgetWarning(int remainingBudget, int totalBudget) {
        // Dismiss any existing dialog
        if (currentLowBudgetDialog != null && currentLowBudgetDialog.isShowing()) {
            currentLowBudgetDialog.dismiss();
        }

        currentLowBudgetDialog = new Dialog(this);
        currentLowBudgetDialog.setContentView(R.layout.dialog_low_budget_warning);
        currentLowBudgetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView warningMessage = currentLowBudgetDialog.findViewById(R.id.warningMessage);
        double percentage = (remainingBudget * 100.0) / totalBudget;
        warningMessage.setText(String.format("Your budget is running low! You have %.1f%% (Php %d) remaining.",
                percentage, remainingBudget));

        Button btnOk = currentLowBudgetDialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            currentLowBudgetDialog.dismiss();
            currentLowBudgetDialog = null;
        });

        currentLowBudgetDialog.show();
    }

    private void showDeleteConfirmationDialog(ExpenseItem expense, int position) {
        // Dismiss any existing dialog
        dismissAllDialogs();

        currentDeleteDialog = new Dialog(this);
        currentDeleteDialog.setContentView(R.layout.dialog_delete_confirmation);
        currentDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnCancel = currentDeleteDialog.findViewById(R.id.btnCancel);
        Button btnConfirmDelete = currentDeleteDialog.findViewById(R.id.btnConfirmDelete);

        btnCancel.setOnClickListener(v -> {
            currentDeleteDialog.dismiss();
            currentDeleteDialog = null;
        });

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
                        currentDeleteDialog.dismiss();
                        currentDeleteDialog = null;

                        // Refresh the activity
                        refreshActivity();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error deleting expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        currentDeleteDialog.dismiss();
                        currentDeleteDialog = null;
                    });
                }
            });
        });

        currentDeleteDialog.show();
    }

    private void setupRecyclerView() {
        expenseRecyclerView.smoothScrollToPosition(0);
        expenseAdapter = new ExpenseAdapter(new ArrayList<>(), (expense, position) -> showDeleteConfirmationDialog(expense, position));
        expenseRecyclerView.setAdapter(expenseAdapter);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add smooth scrolling behavior
        expenseRecyclerView.setHasFixedSize(true);
        expenseRecyclerView.setItemAnimator(null); // Disable default animations for smoother scrolling
    }

    private void dismissAllDialogs() {
        if (currentLowBudgetDialog != null && currentLowBudgetDialog.isShowing()) {
            currentLowBudgetDialog.dismiss();
            currentLowBudgetDialog = null;
        }
        if (currentDateWarningDialog != null && currentDateWarningDialog.isShowing()) {
            currentDateWarningDialog.dismiss();
            currentDateWarningDialog = null;
        }
        if (currentCategoryDialog != null && currentCategoryDialog.isShowing()) {
            currentCategoryDialog.dismiss();
            currentCategoryDialog = null;
        }
        if (currentExpenseDialog != null && currentExpenseDialog.isShowing()) {
            currentExpenseDialog.dismiss();
            currentExpenseDialog = null;
        }
        if (currentSavingsDialog != null && currentSavingsDialog.isShowing()) {
            currentSavingsDialog.dismiss();
            currentSavingsDialog = null;
        }
        if (currentDeleteDialog != null && currentDeleteDialog.isShowing()) {
            currentDeleteDialog.dismiss();
            currentDeleteDialog = null;
        }
    }
}
