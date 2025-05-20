package com.example.Spendly;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener clickListener;
    private OnExpenseClickListener expenseClickListener;

    public interface OnCategoryClickListener {

        void onCategoryClick(Category category, int position);
    }

    public interface OnExpenseClickListener {

        void onExpenseClick(ExpenseItem expense, int position);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.clickListener = listener;
    }

    public void setExpenseClickListener(OnExpenseClickListener listener) {
        this.expenseClickListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryNameView.setText(category.getName());
        holder.amountView.setText(String.format("Php %.2f", category.getTotalAmount()));

        // Setup expenses RecyclerView
        holder.expensesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        ExpenseAdapter expenseAdapter = new ExpenseAdapter(category.getExpenses(), new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(ExpenseItem expense, int position) {
                if (expenseClickListener != null) {
                    expenseClickListener.onExpenseClick(expense, position);
                }
            }
        });
        holder.expensesRecyclerView.setAdapter(expenseAdapter);

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void addCategory(Category category) {
        categories.add(category);
        notifyItemInserted(categories.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    public void removeCategory(int position) {
        if (position >= 0 && position < categories.size()) {
            categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView categoryNameView;
        TextView amountView;
        RecyclerView expensesRecyclerView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryNameView = itemView.findViewById(R.id.tvCategoryName);
            amountView = itemView.findViewById(R.id.tvCategoryTotal);
            expensesRecyclerView = itemView.findViewById(R.id.rvExpenses);
        }
    }
}
