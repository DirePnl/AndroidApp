package com.example.myapplication;

import static com.example.myapplication.R.id.home;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    private ProgressBar budgetProgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.menu) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(getApplicationContext(), MainActivity2.class));
                return true; // Indicate that the item selection was handled
            }
            return false; // Indicate that the item selection was not handled
        });

        // ... rest of your onCreate method






        budgetProgBar = findViewById(R.id.progress_circular);
        ExpenseTarget expenseTarget = new ExpenseTarget();
        budgetProgBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseTarget.show(getSupportFragmentManager(), "dialogbox_expensetarget");
            }
        });

            }

            public void updateBudgetText(String budget) {
                TextView expense = findViewById(R.id.expenseinput);
                expense.setText(budget + " Php");
            }

            public void updateMaxBudget(int budgetMax) {
                int currentProgress = budgetProgBar.getProgress();
                budgetProgBar.setMax(budgetMax);
                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(budgetProgBar, "progress", 0, currentProgress);
                progressAnimator.setDuration(1000);  // 1 second for the animation
                progressAnimator.setInterpolator(new DecelerateInterpolator());  // Optional for smooth deceleration
                progressAnimator.start();
            }
        }       



