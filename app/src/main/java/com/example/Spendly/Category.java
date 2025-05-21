package com.example.Spendly;

import java.util.ArrayList;
import java.util.List;
//Ashley Start Code
public class Category {

    private String id;
    private String name;
    private String label;
    private double totalAmount;
    private List<ExpenseItem> expenses;

    // Required no-arg constructor for Firestore
    public Category() {
        this.expenses = new ArrayList<>();
    }

    // Constructor for creating a new category with name and label
    public Category(String name, String label) {
        this.name = name;
        this.label = label;
        this.totalAmount = 0.0;
        this.expenses = new ArrayList<>();
    }

    // Constructor for loading existing category with name and total amount
    public Category(String name, double totalAmount) {
        this.name = name;
        this.label = name; // Use name as label for existing categories
        this.totalAmount = totalAmount;
        this.expenses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ExpenseItem> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseItem> expenses) {
        this.expenses = expenses;
    }
}
//Ashley End Code