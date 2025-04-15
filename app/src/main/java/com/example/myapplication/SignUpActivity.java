package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth authent;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activity);

        authent = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                if (user.isEmpty()) {
                    signupEmail.setError("Enter Email");
                    return;
                }

                if (password.isEmpty()) {
                    signupPassword.setError("Enter Password");
                    return;
                }

                // Send OTP first, without creating user
                OTP.generateAndStoreOtp(user, firestore, new OTP.OnOtpGenerated() {
                    @Override
                    public void onGenerated(String otp) {
                        // Pass email and password to OTPActivity for verification
                        Intent otpIntent = new Intent(SignUpActivity.this, OTPActivity.class);
                        otpIntent.putExtra("email", user);
                        otpIntent.putExtra("password", password);
                        Toast.makeText(SignUpActivity.this, "OTP sent to your email.", Toast.LENGTH_SHORT).show();
                        startActivity(otpIntent);
                        finish();
                    }

                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(SignUpActivity.this, "Failed to generate OTP: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}
