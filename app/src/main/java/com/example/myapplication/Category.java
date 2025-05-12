package com.example.myapplication;

public class Category {
    private String categoryName;
    private String label;
    private String id;

    // Required no-arg constructor for Firestore
    public Category() {}

    public Category(String categoryName, String label) {
        this.categoryName = categoryName;
        this.label = label;
    }
    public Category(String categoryName, String label, String id) {

        this.categoryName = categoryName;
        this.label = label;
        this.id = id;
    }

    public String getId() {return this.id;}
    public void setId(String id) {
        this.id = id;
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
