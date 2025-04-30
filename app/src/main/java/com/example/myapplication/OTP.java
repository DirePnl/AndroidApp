package com.example.myapplication;

import android.annotation.SuppressLint;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OTP {

    public static void generateAndStoreOtp(String email, FirebaseFirestore firestore, OnOtpGenerated listener) {
        @SuppressLint("DefaultLocale") String otp = String.format("%06d", new Random().nextInt(999999));

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("timestamp", Timestamp.now());

        firestore.collection("email_otps")
                .document(email)
                .set(otpData)
                .addOnSuccessListener(unused -> {
                    listener.onGenerated(otp);
                })
                .addOnFailureListener(e -> listener.onFailed(e.getMessage()));
    }

    public interface OnOtpGenerated {
        void onGenerated(String otp);
        void onFailed(String error);
    }
}