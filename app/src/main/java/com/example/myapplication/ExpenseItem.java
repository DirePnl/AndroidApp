package com.example.myapplication;

public class ExpenseItem {

    private String id; // Firebase document ID
    private String category;
    private double amount;
    private String description;
    private String date;
    private boolean isSavings;

    // Required no-argument constructor for Firebase
    public ExpenseItem() {
    }

    public ExpenseItem(String category, double amount, String description, String date, boolean isSavings) {
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.isSavings = isSavings;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public boolean isSavings() {
        return isSavings;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSavings(boolean savings) {
        isSavings = savings;
    }
}
