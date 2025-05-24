package com.example.Spendly;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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

    public void saveCategory(Category category, CategoryDataCallback callback) {
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
                            category.setId(doc.getId());  // Set the id of the category
                            categories.add(category);
                        }
                    }
                    callback.onCategoriesLoaded(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error loading categories: " + e.getMessage());
                    callback.onError(e);
                });

    }

    public interface DeleteCategoryCallback {

        void onCategoryDeleted();

        void onError(Exception e);
    }

    public void deleteCategory(Category category, DeleteCategoryCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        if (category.getId() == null || category.getId().isEmpty()) {
            callback.onError(new Exception("Category ID is missing"));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("categories")
                .document(category.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseManager", "Category deleted successfully with ID: " + category.getId());
                    callback.onCategoryDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Failed to delete category with ID: " + category.getId(), e);
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

    public interface ExpenseDataCallback {

        void onExpensesLoaded(List<ExpenseItem> expenses);

        void onError(Exception e);
    }

    // Save expense to Firestore
    public void saveExpense(ExpenseItem expense, ExpenseDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w("FirebaseManager", "User not authenticated.");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("expenses")
                .document("transactions")
                .collection("items")
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    // Set the document ID in the expense object
                    expense.setId(documentReference.getId());
                    Log.d("FirebaseManager", "Expense saved successfully with ID: " + documentReference.getId());
                    Toast.makeText(context, "Expense saved!", Toast.LENGTH_SHORT).show();
                    loadExpenses(callback); // Reload expenses after saving
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error saving expense: " + e.getMessage());
                    Toast.makeText(context, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onError(e);
                });
    }

    // Load all expenses from Firestore
    public void loadExpenses(ExpenseDataCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not authenticated."));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("expenses")
                .document("transactions")
                .collection("items")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ExpenseItem> expenses = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ExpenseItem expense = doc.toObject(ExpenseItem.class);
                        if (expense != null) {
                            expense.setId(doc.getId()); // Set the document ID
                            expenses.add(expense);
                        }
                    }
                    callback.onExpensesLoaded(expenses);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error loading expenses: " + e.getMessage());
                    callback.onError(e);
                });
    }

    public interface DeleteExpenseCallback {

        void onExpenseDeleted();

        void onError(Exception e);
    }

    public void deleteExpense(ExpenseItem expense, DeleteExpenseCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        if (expense == null || expense.getId() == null) {
            callback.onError(new IllegalArgumentException("Invalid expense"));
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("expenses")
                .document("transactions")
                .collection("items")
                .document(expense.getId())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onExpenseDeleted())
                .addOnFailureListener(callback::onError);
    }

    public void deleteAllExpenses(DeleteExpenseCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w("FirebaseManager", "User not authenticated.");
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        // Get reference to the expenses collection
        db.collection("users")
                .document(user.getUid())
                .collection("expenses")
                .document("transactions")
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Create a batch to delete all documents
                    WriteBatch batch = db.batch();

                    // Add each document to the batch for deletion
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }

                    // Commit the batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirebaseManager", "All expenses deleted successfully");
                                callback.onExpenseDeleted();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseManager", "Error deleting expenses: " + e.getMessage());
                                callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error getting expenses to delete: " + e.getMessage());
                    callback.onError(e);
                });
    }

    public interface UserProfileCallback {

        void onProfileLoaded(UserProfile profile);

        void onError(Exception e);
    }

    // Save or update user profile
    public void saveUserProfile(UserProfile profile, UserProfileCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not authenticated."));
            return;
        }

        // Save profile using both UID and email for consistency
        db.collection("users")
                .document(user.getUid())
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    // Also save to email-based document
                    db.collection("users")
                            .document(user.getEmail())
                            .set(profile)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("FirebaseManager", "User profile saved successfully.");
                                Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show();
                                loadUserProfile(callback); // Reload profile after saving
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseManager", "Error saving email-based profile: " + e.getMessage());
                                callback.onError(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error saving UID-based profile: " + e.getMessage());
                    Toast.makeText(context, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onError(e);
                });
    }

    // Load user profile from Firestore
    public void loadUserProfile(UserProfileCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User not authenticated."));
            return;
        }

        // Try loading from UID-based document first
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            callback.onProfileLoaded(profile);
                        } else {
                            // If UID-based document fails, try email-based document
                            loadProfileFromEmail(user.getEmail(), callback);
                        }
                    } else {
                        // If UID-based document doesn't exist, try email-based document
                        loadProfileFromEmail(user.getEmail(), callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error loading UID-based profile: " + e.getMessage());
                    // If UID-based load fails, try email-based document
                    loadProfileFromEmail(user.getEmail(), callback);
                });
    }

    private void loadProfileFromEmail(String email, UserProfileCallback callback) {
        db.collection("users")
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            callback.onProfileLoaded(profile);
                        } else {
                            callback.onError(new Exception("Failed to parse user profile data."));
                        }
                    } else {
                        callback.onError(new Exception("User profile not found."));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error loading email-based profile: " + e.getMessage());
                    callback.onError(e);
                });
    }
}
