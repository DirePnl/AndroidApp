package com.example.Spendly;

import android.annotation.SuppressLint;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
//Kyle Start Code
public class OTP {

    private static final long OTP_VALIDITY_DURATION = TimeUnit.MINUTES.toMillis(5); // OTP valid for 5 minutes

    public static void generateAndStoreOtp(String email, FirebaseFirestore firestore, OnOtpGenerated listener) {
        @SuppressLint("DefaultLocale")
        String otp = String.format("%06d", new Random().nextInt(999999));
        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + OTP_VALIDITY_DURATION;

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("timestamp", new Timestamp(new Date(currentTime)));
        otpData.put("expiryTime", new Timestamp(new Date(expiryTime)));
        otpData.put("verified", false);

        firestore.collection("email_otps")
                .document(email)
                .set(otpData)
                .addOnSuccessListener(unused -> {
                    listener.onGenerated(otp);
                })
                .addOnFailureListener(e -> listener.onFailed(e.getMessage()));
    }

    public static void verifyOtp(String email, String enteredOtp, FirebaseFirestore firestore, OnOtpVerified listener) {
        firestore.collection("email_otps")
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onVerified(false, "No OTP found. Please request a new one.");
                        return;
                    }

                    String savedOtp = documentSnapshot.getString("otp");
                    Timestamp expiryTime = documentSnapshot.getTimestamp("expiryTime");
                    boolean isVerified = Boolean.TRUE.equals(documentSnapshot.getBoolean("verified"));

                    if (isVerified) {
                        listener.onVerified(false, "This OTP has already been used.");
                        return;
                    }

                    if (expiryTime == null || expiryTime.toDate().before(new Date())) {
                        listener.onVerified(false, "OTP has expired. Please request a new one.");
                        return;
                    }

                    if (enteredOtp.equals(savedOtp)) {
                        // Mark OTP as verified
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("verified", true);
                        firestore.collection("email_otps")
                                .document(email)
                                .update(updates)
                                .addOnSuccessListener(unused -> listener.onVerified(true, "OTP verified successfully."))
                                .addOnFailureListener(e -> listener.onVerified(false, "Failed to verify OTP: " + e.getMessage()));
                    } else {
                        listener.onVerified(false, "Invalid OTP.");
                    }
                })
                .addOnFailureListener(e -> listener.onVerified(false, "Failed to verify OTP: " + e.getMessage()));
    }

    public interface OnOtpGenerated {

        void onGenerated(String otp);

        void onFailed(String error);
    }

    public interface OnOtpVerified {

        void onVerified(boolean success, String message);
    }
}
//Kyle End Code