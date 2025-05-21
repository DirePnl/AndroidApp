package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//Josiah Start Code
public class EditInfoActivity extends AppCompatActivity {

    private TextInputEditText editUsername, editFirstName, editLastName, editEmail,
            editContactNumber, editDateOfBirth;
    private TextInputLayout usernameLayout, firstNameLayout, lastNameLayout,
            emailLayout, contactNumberLayout, dateOfBirthLayout;
    private FirebaseManager firebaseManager;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogbox_editprofile);

        // Initialize FirebaseManager
        firebaseManager = new FirebaseManager(this);

        // Initialize EditText fields
        editUsername = findViewById(R.id.usernameEditText);
        editFirstName = findViewById(R.id.firstNameEditText);
        editLastName = findViewById(R.id.lastNameEditText);
        editEmail = findViewById(R.id.emailEditText);
        editContactNumber = findViewById(R.id.contactNumEditText);
        editDateOfBirth = findViewById(R.id.dBirthEditText);

        // Initialize TextInputLayouts
        usernameLayout = findViewById(R.id.usernameLayout);
        firstNameLayout = findViewById(R.id.firstNameLayout);
        lastNameLayout = findViewById(R.id.lastNameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        contactNumberLayout = findViewById(R.id.contactNumberLayout);
        dateOfBirthLayout = findViewById(R.id.dateOfBirthLayout);

        MaterialButton doneEditingButton = findViewById(R.id.doneEditingButton);

        // Load existing user data
        loadUserData();

        // Set up date of birth field
        setupDateOfBirthField();

        doneEditingButton.setOnClickListener(v -> {
            if (isProcessing) {
                return;
            }

            if (!validateInputs()) {
                return;
            }

            isProcessing = true;
            doneEditingButton.setEnabled(false);

            // Create UserProfile object
            UserProfile userProfile = new UserProfile(
                    editUsername.getText().toString().trim(),
                    "", // Empty password since we removed the password field
                    editFirstName.getText().toString().trim(),
                    editLastName.getText().toString().trim(),
                    editEmail.getText().toString().trim(),
                    editContactNumber.getText().toString().trim(),
                    editDateOfBirth.getText().toString().trim()
            );

            // Save to Firebase using FirebaseManager
            firebaseManager.saveUserProfile(userProfile, new FirebaseManager.UserProfileCallback() {
                @Override
                public void onProfileLoaded(UserProfile profile) {
                    // Profile saved successfully
                    Intent intent = new Intent(EditInfoActivity.this, ProfileActivity.class);
                    intent.putExtra("username", profile.getUsername());
                    intent.putExtra("firstName", profile.getFirstName());
                    intent.putExtra("lastName", profile.getLastName());
                    intent.putExtra("email", profile.getEmail());
                    intent.putExtra("contactNumber", profile.getContactNumber());
                    intent.putExtra("dateOfBirth", profile.getDateOfBirth());
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    isProcessing = false;
                    doneEditingButton.setEnabled(true);
                    Toast.makeText(EditInfoActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadUserData() {
        firebaseManager.loadUserProfile(new FirebaseManager.UserProfileCallback() {
            @Override
            public void onProfileLoaded(UserProfile profile) {
                editUsername.setText(profile.getUsername());
                editFirstName.setText(profile.getFirstName());
                editLastName.setText(profile.getLastName());
                editEmail.setText(profile.getEmail());
                editContactNumber.setText(profile.getContactNumber());
                editDateOfBirth.setText(profile.getDateOfBirth());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditInfoActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate username
        if (editUsername.getText().toString().trim().isEmpty()) {
            usernameLayout.setError("Username is required");
            isValid = false;
        } else {
            usernameLayout.setError(null);
        }

        // Validate first name
        if (editFirstName.getText().toString().trim().isEmpty()) {
            firstNameLayout.setError("First name is required");
            isValid = false;
        } else {
            firstNameLayout.setError(null);
        }

        // Validate last name
        if (editLastName.getText().toString().trim().isEmpty()) {
            lastNameLayout.setError("Last name is required");
            isValid = false;
        } else {
            lastNameLayout.setError(null);
        }

        // Validate email
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        // Validate contact number
        if (editContactNumber.getText().toString().trim().isEmpty()) {
            contactNumberLayout.setError("Contact number is required");
            isValid = false;
        } else {
            contactNumberLayout.setError(null);
        }

        // Validate date of birth
        if (editDateOfBirth.getText().toString().trim().isEmpty()) {
            dateOfBirthLayout.setError("Date of birth is required");
            isValid = false;
        } else {
            dateOfBirthLayout.setError(null);
        }

        return isValid;
    }

    private void setupDateOfBirthField() {
        editDateOfBirth.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Format the date as DD/MM/YYYY
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(selection));
            editDateOfBirth.setText(formattedDate);
        });

        datePicker.show(getSupportFragmentManager(), "date_picker");
    }
}
//Josiah End Code