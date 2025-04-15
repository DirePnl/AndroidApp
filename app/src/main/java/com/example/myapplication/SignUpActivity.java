package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private EditText signupEmail, signupPassword;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activity);

        firestore = FirebaseFirestore.getInstance();

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(v -> {
            String user = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            if (user.isEmpty()) {
                signupEmail.setError("Enter Email");
            } else if (password.isEmpty()) {
                signupPassword.setError("Enter Password");
            } else {
                // ✅ Generate OTP and send it before account creation
                OTP.generateAndStoreOtp(user, firestore, new OTP.OnOtpGenerated() {
                    @Override
                    public void onGenerated(String otp) {
                        EmailSender.sendEmail(user, otp); // 📧 send OTP to email

                        // ✅ Navigate to OTPActivity with the data
                        Intent intent = new Intent(SignUpActivity.this, OTPActivity.class);
                        intent.putExtra("email", user);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(SignUpActivity.this, "Failed to generate OTP: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
