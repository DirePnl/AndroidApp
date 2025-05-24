package com.example.Spendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class DeleteAccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogbox_delete_account);

        Button cancelButton = findViewById(R.id.cancel_delete_button);
        Button proceedButton = findViewById(R.id.proceed_delete_button);
        EditText del_email = findViewById(R.id.del_email_Edittext);
        EditText del_paw = findViewById(R.id.del_pass_et);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        proceedButton.setOnClickListener(v -> {
            if (del_email.getText().toString().isEmpty()) {
                del_email.setError("Enter email");
            } else if (del_paw.getText().toString().isEmpty()) {
                del_paw.setError("Enter password");
            } else {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(
                            del_email.getText().toString(),
                            del_paw.getText().toString()
                    );

                    user.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // First, delete all user data from Firestore
                                    deleteUserData(user.getUid(), user.getEmail(), () -> {
                                        // Then delete the Firebase Auth user
                                        user.delete()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(DeleteAccountActivity.this,
                                                                "Account deleted successfully",
                                                                Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(DeleteAccountActivity.this,
                                                                task1.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                                } else {
                                    Toast.makeText(DeleteAccountActivity.this,
                                            task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void deleteUserData(String uid, String email, Runnable onComplete) {
        // First get all expenses and categories to delete them
        firestore.collection("users")
                .document(uid)
                .collection("expenses")
                .document("transactions")
                .collection("items")
                .get()
                .addOnSuccessListener(expenseSnapshots -> {
                    // Create a batch for all deletions
                    WriteBatch batch = firestore.batch();

                    // Delete all expense items
                    for (DocumentSnapshot doc : expenseSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    // Delete the transactions document
                    batch.delete(firestore.collection("users")
                            .document(uid)
                            .collection("expenses")
                            .document("transactions"));

                    // Delete the expense target
                    batch.delete(firestore.collection("users")
                            .document(uid)
                            .collection("expenses")
                            .document("expenseTarget"));

                    // Get and delete all categories
                    firestore.collection("users")
                            .document(uid)
                            .collection("categories")
                            .get()
                            .addOnSuccessListener(categorySnapshots -> {
                                // Add category deletions to batch
                                for (DocumentSnapshot doc : categorySnapshots) {
                                    batch.delete(doc.getReference());
                                }

                                // Delete user profile documents (both UID and email-based)
                                batch.delete(firestore.collection("users").document(uid));
                                batch.delete(firestore.collection("users").document(email));

                                // Delete any OTP documents for this email
                                batch.delete(firestore.collection("email_otps").document(email));

                                // Commit all deletions
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("DeleteAccount", "All user data deleted successfully");
                                            onComplete.run();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("DeleteAccount", "Error deleting user data", e);
                                            Toast.makeText(DeleteAccountActivity.this,
                                                    "Error deleting user data: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DeleteAccount", "Error getting categories to delete", e);
                                Toast.makeText(DeleteAccountActivity.this,
                                        "Error deleting user data: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteAccount", "Error getting expenses to delete", e);
                    Toast.makeText(DeleteAccountActivity.this,
                            "Error deleting user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
