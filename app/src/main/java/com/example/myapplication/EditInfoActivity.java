package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class EditInfoActivity extends AppCompatActivity {
//Josiah Code Start Here
    private EditText editUsername, editPassword, editFirstName, editLastName, editEmail,
            editContactNumber, editDateOfBirth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogbox_editprofile);

        // Initialize EditText fields
        editUsername = findViewById(R.id.usernameEditText);
        editPassword = findViewById(R.id.passwordEditText);
        editFirstName = findViewById(R.id.firstNameEditText);
        editLastName = findViewById(R.id.lastNameEditText);
        editEmail = findViewById(R.id.emailEditText);
        editContactNumber = findViewById(R.id.contactNumEditText);
        editDateOfBirth = findViewById(R.id.dBirthEditText);

        ImageButton doneEditingButton = findViewById(R.id.doneEditingButton);
        doneEditingButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditInfoActivity.this, ProfileActivity.class);
            intent.putExtra("username", editUsername.getText().toString());
            intent.putExtra("password", editPassword.getText().toString());
            intent.putExtra("firstName", editFirstName.getText().toString());
            intent.putExtra("lastName", editLastName.getText().toString());
            intent.putExtra("email", editEmail.getText().toString());
            intent.putExtra("contactNumber", editContactNumber.getText().toString());
            intent.putExtra("dateOfBirth", editDateOfBirth.getText().toString());

            startActivity(intent);  // Go back to ProfileActivity
            finish(); // Optional: close EditInfoActivity
        });
    }
//Josiah Code End Here
}