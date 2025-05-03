package com.example.myapplication;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.util.Pair;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseTarget extends AppCompatDialogFragment {
    //for datepicker
    private EditText startDateEditText, endDateEditText;
    public String budgetTF;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogbox_expensetarget, null);

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
            EditText targetBudget = view.findViewById(R.id.targetBudget);
            budgetTF = targetBudget.getText().toString();

            String hasStart = startDateEditText.getText().toString();


            if (budgetTF.isEmpty() || hasStart.isEmpty() ){
                Toast.makeText(getContext(), "Fill all parameters", Toast.LENGTH_SHORT).show();
            }else {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateBudgetText(budgetTF);
                    ((MainActivity) getActivity()).updateMaxBudget(Integer.parseInt(budgetTF));
                }
                dismiss();
            }

        });

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.setTitle("Set Target Budget");
        return dialog;

    }










}
