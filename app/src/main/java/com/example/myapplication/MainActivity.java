package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ProgressBar budgetProgBar;
    private FirebaseFirestore db; // Firestore instance
    private FirebaseAuth mAuth; // FirebaseAuth for getting the current user
    private TextView expenseInputTextView, dateTextView; // Reference for the expense input TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        budgetProgBar = findViewById(R.id.progress_circular);
        expenseInputTextView = findViewById(R.id.expenseinput);
        dateTextView = findViewById(R.id.dateTextView);// Initialize the TextView

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        ExpenseTarget expenseTarget = new ExpenseTarget();

        // Sign in anonymously if the user is not signed in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser signedInUser = FirebaseAuth.getInstance().getCurrentUser();
                            Log.d("AuthCheck", "Signed in anonymously: " + signedInUser.getUid());
                        } else {
                            Log.e("AuthCheck", "Anonymous sign-in failed: " + task.getException());
                        }
                    });
        }

        // Set click listener for the ProgressBar
        budgetProgBar.setOnClickListener(v -> expenseTarget.show(getSupportFragmentManager(), "dialogbox_expensetarget"));

        // Call the method to fetch data when the activity starts
        loadBudgetData();
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

            // Fetching the budget data from Firestore
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
                                updateBudgetText(budget); // Update the TextView with the budget
                                updateMaxBudget(Integer.parseInt(budget));
                                updateDateText(startDate, endDate);// Update the ProgressBar
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



    public void updateDateText(String start, String end){
        dateTextView.setText(start + " - " + end);
    }


    public void updateBudgetText(String budget) {
        // Ensure the TextView is updated with the fetched budget
        expenseInputTextView.setText(budget + " Php");
    }

    public void updateMaxBudget(int budgetMax) {
        // Ensure the ProgressBar is updated correctly
        int currentProgress = budgetProgBar.getProgress();
        budgetProgBar.setMax(budgetMax);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(budgetProgBar, "progress", currentProgress, budgetMax);
        progressAnimator.setDuration(1000); // 1 second for animation
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();
    }

}
