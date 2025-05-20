package com.example.myapplication;

    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.bottomnavigation.BottomNavigationView;

//Josiah Code Start Here
    public class ProfileActivity extends AppCompatActivity {

        private TextView usernameUserInfoTextView, usernameTextView, passwordTextView, firstNameTextView,
                lastNameTextView, emailTextView, contactNumberTextView, dateOfBirthTextView;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);

            //TextViews for the user info
            usernameUserInfoTextView = findViewById(R.id.UsernameTextViewMain2);
            usernameTextView = findViewById(R.id.textview_username);
            passwordTextView = findViewById(R.id.textview_password);
            firstNameTextView = findViewById(R.id.textview_fname);
            lastNameTextView = findViewById(R.id.textview_lname);
            emailTextView = findViewById(R.id.textview_email);
            contactNumberTextView = findViewById(R.id.textview_contactNumber);
            dateOfBirthTextView = findViewById(R.id.textview_dateofbirth);

            // Check if there's updated info from EditInfoActivity
            Intent intent = getIntent();
            if (intent.hasExtra("username")) {
                String username = intent.getStringExtra("username");
                String password = intent.getStringExtra("password");
                String firstName = intent.getStringExtra("firstName");
                String lastName = intent.getStringExtra("lastName");
                String email = intent.getStringExtra("email");
                String contactNumber = intent.getStringExtra("contactNumber");
                String dateOfBirth = intent.getStringExtra("dateOfBirth");
                usernameUserInfoTextView.setText(username);
                usernameTextView.setText(username);
                passwordTextView.setText(password);
                firstNameTextView.setText(firstName);
                lastNameTextView.setText(lastName);
                emailTextView.setText(email);
                contactNumberTextView.setText(contactNumber);
                dateOfBirthTextView.setText(dateOfBirth);

            }

            ImageButton editInfoButton = findViewById(R.id.editInfoButton);//Button to edit info
            Button logoutButton = findViewById(R.id.proceed_delete_button);//Button to logout
            Button deleteAccountButton = findViewById(R.id.delete_account_button);//Button to delete account

            editInfoButton.setOnClickListener(v -> {
                Intent editIntent = new Intent(com.example.myapplication.ProfileActivity.this, EditInfoActivity.class);
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
//Josiah Code End Here
}
