package com.smthbig.shadow;

import android.app.Application;
import com.smthbig.shadow.theme.ThemeManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔥 Apply theme globally BEFORE any activity
        ThemeManager.applyGlobal(this);
    }
}