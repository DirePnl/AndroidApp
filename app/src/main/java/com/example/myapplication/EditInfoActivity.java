package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class EditInfoActivity extends AppCompatActivity {

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
        doneEditingButton.setOnClickListener(v -> returnData());
    }

    private void returnData() {
        Intent resultIntent = new Intent();
        // Pass data back to UserInfoActivity
        resultIntent.putExtra("username", editUsername.getText().toString());
        resultIntent.putExtra("password", editPassword.getText().toString());
        resultIntent.putExtra("firstName", editFirstName.getText().toString());
        resultIntent.putExtra("lastName", editLastName.getText().toString());
        resultIntent.putExtra("email", editEmail.getText().toString());
        resultIntent.putExtra("contactNumber", editContactNumber.getText().toString());
        resultIntent.putExtra("dateOfBirth", editDateOfBirth.getText().toString());

        setResult(RESULT_OK, resultIntent);
        finish(); // Close activity and return to UserInfoActivity
    }
}