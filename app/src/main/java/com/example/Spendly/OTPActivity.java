package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OTPActivity extends AppCompatActivity {

    private EditText otpInput;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userEmail;
    private Button verifyButton;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpInput = findViewById(R.id.otp_input);
        verifyButton = findViewById(R.id.verify_button);
        TextView resendText = findViewById(R.id.resendOtpText);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userEmail = getIntent().getStringExtra("email");

        verifyButton.setOnClickListener(v -> verifyOTP());
        resendText.setOnClickListener(v -> resendOtp());
    }

    private void verifyOTP() {
        if (isProcessing) {
            return;
        }

        String otp = otpInput.getText().toString().trim();

        if (otp.isEmpty()) {
            otpInput.setError("Enter OTP");
            return;
        }

        if (otp.length() != 6) {
            otpInput.setError("OTP must be 6 digits");
            return;
        }

        isProcessing = true;
        verifyButton.setEnabled(false);
        verifyButton.setText("Verifying...");

        // First check if user exists
        Log.d("OTP", "Starting user existence check for email: " + userEmail);

        auth.fetchSignInMethodsForEmail(userEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        Log.d("OTP", "Sign in methods found: " + (signInMethods != null ? signInMethods.toString() : "null"));

                        // Check if user exists by looking for any sign-in methods
                        boolean userExists = signInMethods != null && !signInMethods.isEmpty();
                        Log.d("OTP", "User exists check result: " + userExists);

                        if (userExists) {
                            // User exists
                            Log.d("OTP", "User already exists with methods: " + signInMethods);
                            isProcessing = false;
                            verifyButton.setEnabled(true);
                            verifyButton.setText("Verify");
                            Toast.makeText(OTPActivity.this,
                                    "An account with this email already exists. Please login instead.",
                                    Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(OTPActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        // User doesn't exist, proceed with OTP verification
                        Log.d("OTP", "No existing user found, proceeding with OTP verification");
                        verifyOtpAndProceed(otp);
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e("OTP", "Error checking user existence: " + errorMessage);

                        if (errorMessage.contains("network") || errorMessage.contains("timeout")
                                || errorMessage.contains("unreachable") || errorMessage.contains("interrupted")) {
                            // Network error occurred
                            isProcessing = false;
                            verifyButton.setEnabled(true);
                            verifyButton.setText("Verify");
                            Toast.makeText(OTPActivity.this,
                                    "Network error. Please check your internet connection and try again.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Other error, proceed with OTP verification
                            Log.d("OTP", "Proceeding with OTP verification despite error");
                            verifyOtpAndProceed(otp);
                        }
                    }
                });
    }

    private void verifyOtpAndProceed(String otp) {
        OTP.verifyOtp(userEmail, otp, firestore, new OTP.OnOtpVerified() {
            @Override
            public void onVerified(boolean success, String message) {
                if (success) {
                    Log.d("OTP", "OTP verified successfully");
                    showPasswordSetupDialog(userEmail);
                } else {
                    Log.e("OTP", "OTP verification failed: " + message);
                    isProcessing = false;
                    verifyButton.setEnabled(true);
                    verifyButton.setText("Verify");
                    Toast.makeText(OTPActivity.this,
                            message,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPasswordSetupDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_setup, null);
        builder.setView(dialogView);

        // Set the email in the dialog
        TextView emailDisplay = dialogView.findViewById(R.id.email_display);
        emailDisplay.setText("Email: " + email);

        EditText passwordInput = dialogView.findViewById(R.id.password_input);
        Button createAccountButton = dialogView.findViewById(R.id.create_account_button);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        createAccountButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();
            if (password.length() < 6) {
                Toast.makeText(OTPActivity.this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            createAccount(email, password);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createAccount(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignUp", "Account created successfully");
                        // Create user document in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("createdAt", new Date());

                        firestore.collection("users")
                                .document(email)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("SignUp", "User document created");
                                    Toast.makeText(OTPActivity.this,
                                            "Account created successfully!",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(OTPActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SignUp", "Error creating user document", e);
                                    Toast.makeText(OTPActivity.this,
                                            "Error creating account: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("SignUp", "Error creating account", task.getException());
                        Toast.makeText(OTPActivity.this,
                                "Error creating account: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOtp() {
        if (isProcessing) {
            return;
        }

        // User doesn't exist, proceed with resending OTP
        OTP.generateAndStoreOtp(userEmail, firestore, new OTP.OnOtpGenerated() {
            @Override
            public void onGenerated(String otp) {
                Log.d("OTP", "Generated OTP: " + otp);
                EmailSender.sendEmail(userEmail, otp);
                Toast.makeText(OTPActivity.this, "OTP Resent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String error) {
                Toast.makeText(OTPActivity.this,
                        "Failed to resend OTP: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
