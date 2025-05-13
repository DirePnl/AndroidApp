package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class UserInfoActivity extends AppCompatActivity {

    private TextView usernameTextView, passwordTextView, firstNameTextView, lastNameTextView,
            emailTextView, contactNumberTextView, dateOfBirthTextView;

    // ActivityResultLauncher to receive data back
    private final ActivityResultLauncher<Intent> editInfoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    String username = data.getStringExtra("username");
                    String firstName = data.getStringExtra("firstName");
                    String lastName = data.getStringExtra("lastName");
                    String email = data.getStringExtra("email");
                    String contactNumber = data.getStringExtra("contactNumber");
                    String dateOfBirth = data.getStringExtra("dateOfBirth");

                    // Update the UI
                    usernameTextView.setText(username);
                    emailTextView.setText(email);
                }
            });

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


        ImageButton editInfoButton = findViewById(R.id.editInfoButton);//Button to edit info
        Button logoutButton = findViewById(R.id.proceed_delete_button);//Button to logout
        Button deleteAccountButton = findViewById(R.id.delete_account_button);//Button to delete account

        editInfoButton.setOnClickListener(v -> editInfo());
        logoutButton.setOnClickListener(v -> logout());
        deleteAccountButton.setOnClickListener(v -> deleteAccount());
    }

    private void editInfo() {
        Intent intent = new Intent(this, EditInfoActivity.class);
        editInfoLauncher.launch(intent); // Launch for result
    }

    private void logout() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void deleteAccount() {
        startActivity(new Intent(this, DeleteAccountActivity.class));
    }
}
