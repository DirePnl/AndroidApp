package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class DeleteAccountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogbox_delete_account);

        Button cancelButton = findViewById(R.id.cancel_delete_button);
        Button proceedButton = findViewById(R.id.proceed_delete_button);

        proceedButton.setOnClickListener(v -> {
            // TODO: Add account deletion logic here (e.g., from database)

            // Return to LoginActivity and clear back stack
            Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cancelButton.setOnClickListener(v -> {
            finish(); // Close the activity and return to the previous screen
        });
    }

}
