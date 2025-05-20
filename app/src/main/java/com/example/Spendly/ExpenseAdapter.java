package com.example.Spendly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<ExpenseItem> expenses;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {

        void onExpenseClick(ExpenseItem expense, int position);
    }

    public ExpenseAdapter(List<ExpenseItem> expenses, OnExpenseClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseItem expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void clearExpenses() {
        expenses.clear();
        notifyDataSetChanged();
    }

    public void addExpense(ExpenseItem expense) {
        expenses.add(expense);
        notifyItemInserted(expenses.size() - 1);
    }

    public List<ExpenseItem> getExpenseList() {
        return new ArrayList<>(expenses); // Return a copy to prevent external modifications
    }

    public void removeExpense(int position) {
        if (position >= 0 && position < expenses.size()) {
            expenses.remove(position);
            notifyItemRemoved(position);
        }
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDescription;
        private TextView tvAmount;
        private ImageView ivDelete;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            ivDelete = itemView.findViewById(R.id.ivDeleteExpense);

            ivDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onExpenseClick(expenses.get(position), position);
                }
            });
        }

        void bind(ExpenseItem expense) {
            tvDescription.setText(expense.getDescription());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            tvAmount.setText(formatter.format(expense.getAmount()));
        }
    }
}
