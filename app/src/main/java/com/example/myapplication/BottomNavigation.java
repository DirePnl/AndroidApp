package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class BottomNavigation {
    @SuppressLint("NonConstantResourceId")
    public static boolean handleNavigation(Context context, int itemId) {
        Intent intent = null;

        if (itemId == R.id.home) {
            intent = new Intent(context, MainActivity.class);
        } else if (itemId == R.id.menu) {
            intent = new Intent(context, MainActivity2.class);
        } else if (itemId == R.id.profile) {
            intent = new Intent(context, ProfileActivity.class);
        }

        if (intent != null && !(context instanceof Activity &&
                intent.getComponent().getClassName().equals(context.getClass().getName()))) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }
}