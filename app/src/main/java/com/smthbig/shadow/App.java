package com.smthbig.shadow;

import android.app.Application;

import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.theme.ThemeManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator.init(this);
        ThemeManager.applyGlobal(this);
    }
}
