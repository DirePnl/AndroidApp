package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;
//Kyle Start Code
public class SignUpActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private TextView loginRedirectText;
    private Button signupButton;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activity);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(v -> {
            if (isProcessing) {
                return; // Prevent multiple clicks
            }

            String user = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            if (user.isEmpty()) {
                signupEmail.setError("Enter Email");
            } else if (password.isEmpty()) {
                signupPassword.setError("Enter Password");
            } else {
                isProcessing = true;
                signupButton.setEnabled(false);
                signupButton.setText("Checking...");

                // First try to log in to verify if account exists
                auth.signInWithEmailAndPassword(user, password)
                        .addOnCompleteListener(loginTask -> {
                            if (loginTask.isSuccessful()) {
                                // User exists and can log in
                                isProcessing = false;
                                signupButton.setEnabled(true);
                                signupButton.setText("Sign Up");
                                Toast.makeText(SignUpActivity.this,
                                        "An account with this email already exists. Please login instead.",
                                        Toast.LENGTH_LONG).show();
                                // Sign out since we only wanted to check
                                auth.signOut();
                                return;
                            }

                            // If login fails, check if it's because user doesn't exist
                            if (loginTask.getException() instanceof FirebaseAuthInvalidUserException) {
                                // User doesn't exist, proceed with normal checks
                                Log.d("SignUp", "Login attempt failed - user doesn't exist, proceeding with checks");
                                checkUserExistence(user, password);
                            } else {
                                // Some other error occurred
                                isProcessing = false;
                                signupButton.setEnabled(true);
                                signupButton.setText("Sign Up");
                                Toast.makeText(SignUpActivity.this,
                                        "Error checking account: " + loginTask.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        loginRedirectText.setOnClickListener(v
                -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }

    private void checkUserExistence(String email, String password) {
        // Check if user exists in Firebase Auth
        Log.d("SignUp", "Checking Firebase Auth for email: " + email);
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignUp", "Firebase Auth check successful");
                        if (task.getResult().getSignInMethods() != null
                                && !task.getResult().getSignInMethods().isEmpty()) {
                            // User already exists
                            Log.d("SignUp", "User exists in Firebase Auth");
                            isProcessing = false;
                            signupButton.setEnabled(true);
                            signupButton.setText("Sign Up");
                            Toast.makeText(SignUpActivity.this,
                                    "An account with this email already exists. Please login instead.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.d("SignUp", "User not found in Firebase Auth, checking Firestore");

                        // If user doesn't exist, check Firestore
                        firestore.collection("users")
                                .document(email)
                                .get()
                                .addOnCompleteListener(firestoreTask -> {
                                    isProcessing = false;
                                    signupButton.setEnabled(true);
                                    signupButton.setText("Sign Up");

                                    if (firestoreTask.isSuccessful()) {
                                        Log.d("SignUp", "Firestore check successful");
                                        if (firestoreTask.getResult().exists()) {
                                            // User exists in Firestore
                                            Log.d("SignUp", "User exists in Firestore");
                                            Toast.makeText(SignUpActivity.this,
                                                    "An account with this email already exists. Please login instead.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            // User doesn't exist anywhere, proceed with OTP
                                            Log.d("SignUp", "User not found in Firestore, proceeding with OTP");
                                            proceedWithOTP(email, password);
                                        }
                                    } else {
                                        Log.e("SignUp", "Firestore check failed: " + firestoreTask.getException().getMessage());
                                        Toast.makeText(SignUpActivity.this,
                                                "Error checking email: " + firestoreTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e("SignUp", "Firebase Auth check failed: " + task.getException().getMessage());
                        isProcessing = false;
                        signupButton.setEnabled(true);
                        signupButton.setText("Sign Up");
                        Toast.makeText(SignUpActivity.this,
                                "Error checking email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedWithOTP(String email, String password) {
        Log.d("SignUp", "Starting final checks before OTP generation for: " + email);
        // Double check with Firebase Auth one more time before proceeding
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignUp", "Final Firebase Auth check successful");
                        if (task.getResult().getSignInMethods() != null
                                && !task.getResult().getSignInMethods().isEmpty()) {
                            // Email exists in Firebase Auth
                            Log.d("SignUp", "User found in final Firebase Auth check");
                            isProcessing = false;
                            signupButton.setEnabled(true);
                            signupButton.setText("Sign Up");
                            Toast.makeText(SignUpActivity.this,
                                    "An account with this email already exists. Please login instead.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Check Firestore one more time
                        Log.d("SignUp", "User not found in final Firebase Auth check, checking Firestore");
                        firestore.collection("users")
                                .document(email)
                                .get()
                                .addOnCompleteListener(firestoreTask -> {
                                    if (firestoreTask.isSuccessful()) {
                                        Log.d("SignUp", "Final Firestore check successful");
                                        if (firestoreTask.getResult().exists()) {
                                            // User exists in Firestore
                                            Log.d("SignUp", "User found in final Firestore check");
                                            isProcessing = false;
                                            signupButton.setEnabled(true);
                                            signupButton.setText("Sign Up");
                                            Toast.makeText(SignUpActivity.this,
                                                    "An account with this email already exists. Please login instead.",
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        // Final check passed, proceed with OTP
                                        Log.d("SignUp", "All checks passed, proceeding with OTP generation");
                                        OTP.generateAndStoreOtp(email, firestore, new OTP.OnOtpGenerated() {
                                            @Override
                                            public void onGenerated(String otp) {
                                                Log.d("SignUp", "OTP generated successfully");
                                                EmailSender.sendEmail(email, otp);
                                                Intent intent = new Intent(SignUpActivity.this, OTPActivity.class);
                                                intent.putExtra("email", email);
                                                intent.putExtra("password", password);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onFailed(String error) {
                                                Log.e("SignUp", "OTP generation failed: " + error);
                                                isProcessing = false;
                                                signupButton.setEnabled(true);
                                                signupButton.setText("Sign Up");
                                                Toast.makeText(SignUpActivity.this,
                                                        "Failed to generate OTP: " + error,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.e("SignUp", "Final Firestore check failed: " + firestoreTask.getException().getMessage());
                                        isProcessing = false;
                                        signupButton.setEnabled(true);
                                        signupButton.setText("Sign Up");
                                        Toast.makeText(SignUpActivity.this,
                                                "Error checking email: " + firestoreTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e("SignUp", "Final Firebase Auth check failed: " + task.getException().getMessage());
                        isProcessing = false;
                        signupButton.setEnabled(true);
                        signupButton.setText("Sign Up");
                        Toast.makeText(SignUpActivity.this,
                                "Error checking email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
//Kyle End Code