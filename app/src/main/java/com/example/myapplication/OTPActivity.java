package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
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

        firestore.collection("email_otps")
                .document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String savedOtp = documentSnapshot.getString("otp");
                        if (enteredOtp.equals(savedOtp)) {
                            Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();

                            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No OTP found. Try resending.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to verify OTP", Toast.LENGTH_SHORT).show());
    }

    private void resendOtp() {
        OTP.generateAndStoreOtp(userEmail, firestore, new OTP.OnOtpGenerated() {
            @Override
            public void onGenerated(String otp) {
                Toast.makeText(OTPActivity.this, "OTP Resent: ", Toast.LENGTH_SHORT).show(); // Testing only
            }

            @Override
            public void onFailed(String error) {
                Toast.makeText(OTPActivity.this, "Failed to resend OTP: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
