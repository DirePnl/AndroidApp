    package com.example.myapplication;

    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import androidx.appcompat.app.AppCompatActivity;

    public class UserInfoActivity extends AppCompatActivity {

        private TextView usernameTextView, passwordTextView, firstNameTextView, lastNameTextView,
                emailTextView, contactNumberTextView, dateOfBirthTextView;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_info);

            //TextViews for the user info
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
                Intent editIntent = new Intent(UserInfoActivity.this, EditInfoActivity.class);
                startActivity(editIntent);
            });

            logoutButton.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
            });

            deleteAccountButton.setOnClickListener(v -> {
                startActivity(new Intent(this, DeleteAccountActivity.class));
            });
        }

    }
