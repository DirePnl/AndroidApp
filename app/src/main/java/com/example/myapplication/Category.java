package com.example.myapplication;

public class Category {
    private String categoryName;
    private String label;

    public Category(String categoryName, String label) {
        this.categoryName = categoryName;
        this.label = label;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
