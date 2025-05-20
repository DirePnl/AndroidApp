package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

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

        // Add TextWatcher for date formatting (dd/mm/yyyy) with day/month validation
        editDateOfBirth.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private final Calendar cal = Calendar.getInstance();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    String cleanC = current.replaceAll("[^\\d]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    boolean invalidDay = false;
                    boolean invalidMonth = false;

                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int mon = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));
                        if (day > 31) invalidDay = true;
                        if (mon > 12) invalidMonth = true;
                        cal.set(Calendar.DAY_OF_MONTH, day);
                        cal.set(Calendar.MONTH, mon - 1);
                        cal.set(Calendar.YEAR, year);
                    }

                    clean = String.format("%s/%s/%s",
                            clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8)
                    );

                    current = clean;
                    editDateOfBirth.setText(current);
                    editDateOfBirth.setSelection(sel < current.length() ? sel : current.length());

                    // Show warnings
                    if (invalidDay) {
                        editDateOfBirth.setError("Day cannot exceed 31");
                    } else if (invalidMonth) {
                        editDateOfBirth.setError("Month cannot exceed 12");
                    } else {
                        editDateOfBirth.setError(null); // Clear error if valid
                    }
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        doneEditingButton.setOnClickListener(v -> {
            String email = editEmail.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Please enter a valid email address");
                Toast.makeText(EditInfoActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }
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