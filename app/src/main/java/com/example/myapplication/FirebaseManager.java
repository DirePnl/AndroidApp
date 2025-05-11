package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirebaseManager {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;

    public FirebaseManager(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    // Save or update fixed budget target document
    public void saveBudgetTarget(UserData target) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w("FirebaseManager", "User not authenticated.");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("expenses")
                .document("expenseTarget")
                .set(target)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseManager", "Budget saved successfully.");
                    Toast.makeText(context, "Budget saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error saving budget: " + e.getMessage());
                    Toast.makeText(context, "Failed to save budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Save a new category (adds a new document)
    // Modified saveCategory method with a callback to notify after saving
    public void saveCategory(Category category, FirebaseManager.CategoryDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w("FirebaseManager", "User not authenticated.");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("categories")
                .add(category)
                .addOnSuccessListener(ref -> {
                    Log.d("FirebaseManager", "Category saved successfully.");
                    Toast.makeText(context, "Category added!", Toast.LENGTH_SHORT).show();
                    loadCategories(callback); // Reload categories after saving
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error saving category: " + e.getMessage());
                    Toast.makeText(context, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onError(e); // Pass error to the callback
                });
    }


    public interface CategoryDataCallback {
        void onCategoriesLoaded(List<Category> categories);
        void onError(Exception e);
    }

    // Load all categories from Firestore
    public void loadCategories(CategoryDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not authenticated."));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("categories")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Category> categories = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Category category = doc.toObject(Category.class);
                        if (category != null) {
                            categories.add(category);
                        } else {
                            Log.w("FirebaseManager", "Null category document found.");
                        }
                    }
                    callback.onCategoriesLoaded(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error loading categories: " + e.getMessage());
                    callback.onError(e);
                });
    }

    // Sign in anonymously if needed
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public void signInAnonymouslyIfNeeded(AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            callback.onSuccess(user);
        } else {
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser signedInUser = authResult.getUser();
                        if (signedInUser != null) {
                            Log.d("FirebaseManager", "Signed in anonymously as: " + signedInUser.getUid());
                            callback.onSuccess(signedInUser);
                        } else {
                            callback.onFailure(new Exception("Anonymous sign-in returned null user."));
                        }
                    })
                    .addOnFailureListener(callback::onFailure);
        }
    }
}
