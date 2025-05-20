package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.profile) {
                // Already on the home page, no action needed
                return true;
            } else if (itemId == R.id.menu) {
                // Navigate to MainActivity2 (Menu page)
                startActivity(new Intent(ProfileActivity.this, MenuActivity.class));
                return true; // Indicate that the item selection was handled
            } else if (itemId == R.id.home) {
                // Navigate to Home page
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true; // Indicate that the item selection was handled
            }
            return false; // Indicate that the item selection was not handled
        });

    }

}
