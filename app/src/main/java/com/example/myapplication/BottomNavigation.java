package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class BottomNavigation {

    @SuppressLint("NonConstantResourceId")
    public static boolean handleNavigation(Context context, int itemId) {
        Intent intent = null;

        // Handling navigation for different items
        if (itemId == R.id.home) {
            // Create an Intent for MainActivity
            intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        } else if (itemId == R.id.menu) {
            // Create an Intent for MenuActivity
            intent = new Intent(context, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        } else if (itemId == R.id.profile) {
            // Create an Intent for ProfileActivity
            intent = new Intent(context, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // Check if the intent is valid and the current context is not the same activity
        if (intent != null && !(context instanceof Activity &&
                intent.getComponent().getClassName().equals(context.getClass().getName()))) {

            // Start the activity
            context.startActivity(intent);

            // Return true to indicate that navigation was successful
            return true;
        }

        // Return false if no valid intent or same activity
        return false;
    }
}