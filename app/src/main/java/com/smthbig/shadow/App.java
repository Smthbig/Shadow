package com.smthbig.shadow;

import android.app.Application;
import android.content.Intent;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.tracking.UsageMonitorService;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔥 Apply theme globally BEFORE any activity
        ThemeManager.applyGlobal(this);

        // 🔥 Start enforcement service
        startService(new Intent(this, UsageMonitorService.class));
    }
}