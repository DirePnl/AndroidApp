package com.example.myapplication;

public class UserData {
    public String budget;
    public String startDate;
    public String endDate;
    public String note;

    // Default constructor required for Firebase
    public UserData() {
    }

    public UserData(String budget, String startDate, String endDate, String note) {
        this.budget = budget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
    }

    // Getters and setters
    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
