package com.example.Spendly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.util.Pair;

import com.example.myapplication.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseTarget extends AppCompatDialogFragment {

    //for datepicker
    UserData target;
    public EditText startDateEditText, endDateEditText;
    public String budgetTF;
    private FirebaseManager firebaseManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogbox_expensetarget, null);
        firebaseManager = new FirebaseManager(getContext());

        startDateEditText = view.findViewById(R.id.editTextStartDate);
        endDateEditText = view.findViewById(R.id.editTextEndDate);
        Button targetButton = view.findViewById(R.id.targetBudgetButton);

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range");

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        View.OnClickListener calendarClickListener = v -> picker.show(getParentFragmentManager(), "date_picker");

        startDateEditText.setOnClickListener(calendarClickListener);
        endDateEditText.setOnClickListener(calendarClickListener);

        picker.addOnPositiveButtonClickListener(selection -> {
            long startMillis = selection.first;
            long endMillis = selection.second;

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            startDateEditText.setText(sdf.format(new Date(startMillis)));
            endDateEditText.setText(sdf.format(new Date(endMillis)));
        });

        targetButton.setOnClickListener(v -> {
            try {
                EditText targetBudget = view.findViewById(R.id.targetBudget);
                budgetTF = targetBudget.getText().toString();

                String start = startDateEditText.getText().toString();
                String end = endDateEditText.getText().toString();

                // Check if inputs are valid
                if (budgetTF.isEmpty() || start.isEmpty() || end.isEmpty()) {
                    Toast.makeText(getContext(), "Fill all parameters", Toast.LENGTH_SHORT).show();
                } else {
                    // Show confirmation dialog
                    showBudgetChangeConfirmationDialog(budgetTF, start, end);
                }
            } catch (Exception e) {
                Log.e("ExpenseTarget", "Error saving budget target: " + e.getMessage(), e);
                Toast.makeText(getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setTitle("Set Target Budget");
        return dialog;
    }

    private void showBudgetChangeConfirmationDialog(String budget, String start, String end) {
        Dialog confirmDialog = new Dialog(requireContext());
        confirmDialog.setContentView(R.layout.dialog_budget_change_confirmation);
        confirmDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button btnCancel = confirmDialog.findViewById(R.id.btnCancel);
        Button btnConfirm = confirmDialog.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> confirmDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            // Delete all expenses first
            firebaseManager.deleteAllExpenses(new FirebaseManager.DeleteExpenseCallback() {
                @Override
                public void onExpenseDeleted() {
                    // Create and save the new budget target
                    target = new UserData(budget, start, end, "");
                    firebaseManager.saveBudgetTarget(target);

                    // Update MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateBudgetText(budget);
                        ((MainActivity) getActivity()).updateMaxBudget(Integer.parseInt(budget));
                        ((MainActivity) getActivity()).updateDateText(start, end);
                    }

                    confirmDialog.dismiss();
                    dismiss();
                    reset();// Dismiss the budget target dialog
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    confirmDialog.dismiss();
                }
            });

        });

        confirmDialog.show();
    }


    private void reset() {
        Context context = getContext(); // or getActivity()

        if (context != null) {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);

            // Optionally, if you want to close the current Activity hosting this dialog:
            if (getActivity() != null) {
                getActivity().finish();
            }
        } else {
            Log.e("ExpenseTarget", "Context is null, cannot start MainActivity");
        }
    }

}
