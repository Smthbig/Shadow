package com.smthbig.shadow.launcher.core;

import android.content.Context;
import android.content.Intent;

public class AppLauncher {

    private final Context context;

    public AppLauncher(Context context) {
        this.context = context.getApplicationContext();
    }

    public void launch(Intent intent) {
        if (intent == null) return;
        
        try {
            // Ensure every launch is isolated and robust
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            context.startActivity(intent);
        } catch (Exception ignored) {
            // Log or handle launch failure if needed for v1.1
        }
    }
}
