package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameUserInfoTextView, usernameTextView, firstNameTextView,
            lastNameTextView, emailTextView, contactNumberTextView, dateOfBirthTextView;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize FirebaseManager
        firebaseManager = new FirebaseManager(this);

        //TextViews for the user info
        usernameUserInfoTextView = findViewById(R.id.UsernameTextViewMain2);
        usernameTextView = findViewById(R.id.textview_username);
        firstNameTextView = findViewById(R.id.textview_fname);
        lastNameTextView = findViewById(R.id.textview_lname);
        emailTextView = findViewById(R.id.textview_email);
        contactNumberTextView = findViewById(R.id.textview_contactNumber);
        dateOfBirthTextView = findViewById(R.id.textview_dateofbirth);

        // Load user profile data from Firebase
        loadUserProfile();

        ImageButton editInfoButton = findViewById(R.id.editInfoButton);//Button to edit info
        Button logoutButton = findViewById(R.id.proceed_delete_button);//Button to logout
        Button deleteAccountButton = findViewById(R.id.delete_account_button);//Button to delete account

        editInfoButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(ProfileActivity.this, EditInfoActivity.class);
            startActivity(editIntent);
        });

        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        deleteAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DeleteAccountActivity.class));
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.profile) {
                return true;
            } else if (itemId == R.id.menu) {
                intent = new Intent(ProfileActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.home) {
                intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            if (intent != null) {
                // Clear the back stack and start a new task
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Prevent stacking
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when activity resumes
        loadUserProfile();
    }

    private void loadUserProfile() {
        firebaseManager.loadUserProfile(new FirebaseManager.UserProfileCallback() {
            @Override
            public void onProfileLoaded(UserProfile profile) {
                // Update UI with profile data
                usernameUserInfoTextView.setText(profile.getUsername());
                usernameTextView.setText(profile.getUsername());
                firstNameTextView.setText(profile.getFirstName());
                lastNameTextView.setText(profile.getLastName());
                emailTextView.setText(profile.getEmail());
                contactNumberTextView.setText(profile.getContactNumber());
                dateOfBirthTextView.setText(profile.getDateOfBirth());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
