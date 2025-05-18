package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseClickListener {

        void onExpenseClick(ExpenseItem expense, int position);
    }

    private List<ExpenseItem> expenseList;
    private OnExpenseClickListener clickListener;

    public ExpenseAdapter(OnExpenseClickListener listener) {
        expenseList = new ArrayList<>();
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseItem expense = expenseList.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onExpenseClick(expense, position);
            }
        });
        String displayText = String.format("%s - Php %.2f", expense.getCategory(), expense.getAmount());
        holder.categoryNameView.setText(displayText);
        holder.labelView.setText(String.format("%s - %s", expense.getDescription(), expense.getDate()));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void addExpense(ExpenseItem expense) {
        expenseList.add(expense);
        notifyItemInserted(expenseList.size() - 1);
    }

    public void clearExpenses() {
        expenseList.clear();
        notifyDataSetChanged();
    }

    public void removeExpense(int position) {
        if (position >= 0 && position < expenseList.size()) {
            expenseList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, expenseList.size());
        }
    }

    public List<ExpenseItem> getExpenseList() {
        return new ArrayList<>(expenseList); // Return a copy to prevent external modifications
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        final TextView categoryNameView;
        final TextView labelView;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            categoryNameView = itemView.findViewById(R.id.tvCategoryName);
            labelView = itemView.findViewById(R.id.tvLabel);
        }
    }
}
