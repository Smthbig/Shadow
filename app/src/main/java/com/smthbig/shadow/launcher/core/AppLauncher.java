package com.smthbig.shadow.system;

import android.content.Context;
import android.content.Intent;

public class AppLauncher {

    private final Context context;

    public AppLauncher(Context context) {
        this.context = context.getApplicationContext();
    }

    public void launch(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }
}