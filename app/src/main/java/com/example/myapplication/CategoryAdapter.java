package com.example.myapplication;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;


    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(itemView);
    }







    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = categories.get(position);
        if (currentCategory != null) {
            holder.tvCategoryName.setText(currentCategory.getCategoryName());
            holder.tvLabel.setText(currentCategory.getLabel());

            holder.ivDelete.setOnClickListener(v -> {
                Log.d("CategoryAdapter", "Delete clicked for category ID: " + currentCategory.getId());

                FirebaseManager firebaseManager = new FirebaseManager(holder.itemView.getContext());
                firebaseManager.deleteCategory(currentCategory.getId(), new FirebaseManager.CategoryDataCallback() {
                    @Override
                    public void onCategoriesLoaded(List<Category> updatedCategories) {
                        Log.d("CategoryAdapter", "Categories updated after deletion");
                        setCategories(updatedCategories); // Refresh the adapter
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("CategoryAdapter", "Error deleting category: " + e.getMessage());
                    }
                });
            });


        }
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
        notifyDataSetChanged(); // Make sure to notify the adapter to refresh
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategoryName;
        public TextView tvLabel;

        public ImageView ivDelete;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

    }
}