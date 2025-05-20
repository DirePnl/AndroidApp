package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OTPActivity extends AppCompatActivity {

    private EditText otpInput;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userEmail;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpInput = findViewById(R.id.otp_input);
        Button verifyButton = findViewById(R.id.verify_button);
        TextView resendText = findViewById(R.id.resendOtpText);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userEmail = getIntent().getStringExtra("email");
        userPassword = getIntent().getStringExtra("password");

        verifyButton.setOnClickListener(v -> verifyOtp());
        resendText.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {
        String enteredOtp = otpInput.getText().toString().trim();
        if (enteredOtp.isEmpty()) {
            otpInput.setError("Enter OTP");
            return;
        }

        // First check if user already exists
        auth.fetchSignInMethodsForEmail(userEmail)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        if (authTask.getResult().getSignInMethods() != null
                                && !authTask.getResult().getSignInMethods().isEmpty()) {
                            // User already exists
                            Toast.makeText(this, "An account with this email already exists. Please login instead.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                            return;
                        }

                        // If user doesn't exist, proceed with OTP verification
                        OTP.verifyOtp(userEmail, enteredOtp, firestore, new OTP.OnOtpVerified() {
                            @Override
                            public void onVerified(boolean success, String message) {
                                if (success) {
                                    // Double check user doesn't exist before creating
                                    auth.fetchSignInMethodsForEmail(userEmail)
                                            .addOnCompleteListener(finalCheck -> {
                                                if (finalCheck.isSuccessful()) {
                                                    if (finalCheck.getResult().getSignInMethods() != null
                                                            && !finalCheck.getResult().getSignInMethods().isEmpty()) {
                                                        // User was created between checks
                                                        Toast.makeText(OTPActivity.this,
                                                                "An account with this email already exists. Please login instead.",
                                                                Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(OTPActivity.this, LoginActivity.class));
                                                        finish();
                                                        return;
                                                    }

                                                    // Create user account
                                                    auth.createUserWithEmailAndPassword(userEmail, userPassword)
                                                            .addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(OTPActivity.this,
                                                                            "Account created successfully!",
                                                                            Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(OTPActivity.this, LoginActivity.class));
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(OTPActivity.this,
                                                                            "Account creation failed: " + task.getException().getMessage(),
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(OTPActivity.this,
                                                            "Error verifying account status: " + finalCheck.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(OTPActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(this,
                                "Error checking account status: " + authTask.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOtp() {
        // Check if user exists before resending OTP
        auth.fetchSignInMethodsForEmail(userEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods() != null
                                && !task.getResult().getSignInMethods().isEmpty()) {
                            // User already exists
                            Toast.makeText(this,
                                    "An account with this email already exists. Please login instead.",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
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
                    } else {
                        Toast.makeText(this,
                                "Error checking account status: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
