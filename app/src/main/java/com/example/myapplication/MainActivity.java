package com.example.myapplication;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private ProgressBar budgetProgBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}