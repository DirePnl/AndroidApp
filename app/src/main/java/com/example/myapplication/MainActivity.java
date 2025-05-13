package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ProgressBar budgetProgBar;
    private FirebaseFirestore db;
    private TextView expenseInputTextView, dateTextView;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        firebaseManager = new FirebaseManager(this);

        budgetProgBar = findViewById(R.id.progress_circular);
        expenseInputTextView = findViewById(R.id.expenseinput);
        dateTextView = findViewById(R.id.dateTextView);

        db = FirebaseFirestore.getInstance();

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

        bottomNavigationView.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.menu) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                return true; // Indicate that the item selection was handled
            }else if (itemId == R.id.profile) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                return true; // Indicate that the item selection was handled
            }
            return false; // Indicate that the item selection was not handled
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        loadBudgetData();
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
        expenseInputTextView.setText(budget + " Php");
    }

    public void updateMaxBudget(int budgetMax) {
        int currentProgress = budgetProgBar.getProgress();
        budgetProgBar.setMax(budgetMax);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(budgetProgBar, "progress", currentProgress, budgetMax);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }
}
