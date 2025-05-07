package com.example.myapplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ImageButton viewUserInfoButton = findViewById(R.id.infoBtn);

        viewUserInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoActivity();
            }
        });
    }

    private void infoActivity() {
        // Start the InfoActivity
        Intent intent = new Intent(this, UserInfoActivity.class);
        startActivity(intent);
    }
}
