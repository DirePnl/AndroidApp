package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;

    public FirebaseManager(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    public void saveBudgetTarget(UserData target) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Save to a fixed document so it updates, not creates new ones
            db.collection("users")
                    .document(uid)
                    .collection("expenses")
                    .document("expenseTarget")
                    .set(target)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseManager", "Budget saved successfully.");
                        Toast.makeText(context, "Budget saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseManager", "Error saving budget: " + e.getMessage());
                        Toast.makeText(context, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.w("FirebaseManager", "User not authenticated.");
        }
    }
}
