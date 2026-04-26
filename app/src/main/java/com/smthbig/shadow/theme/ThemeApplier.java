package com.smthbig.shadow.theme;

import android.app.Activity;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.color.DynamicColors;

final class ThemeApplier {

    private ThemeApplier() {}

    static void apply(Activity activity, String mode, String backgroundType) {

        int targetNightMode = ThemeConfig.getNightMode(mode);

        if (AppCompatDelegate.getDefaultNightMode() != targetNightMode) {
            AppCompatDelegate.setDefaultNightMode(targetNightMode);
        }

        activity.setTheme(ThemeConfig.getPerfectTheme(mode, backgroundType));

        applySystemUi(activity, mode);

        if (ThemeMode.DYNAMIC.equals(mode) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(activity);
        }
    }

    private static void applySystemUi(Activity activity, String mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = activity.getWindow();

            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Icon Brightness Logic
            boolean isLightIconsNeeded = 
                    ThemeMode.LIGHT.equals(mode) || 
                    ThemeMode.GLASS_LIGHT.equals(mode) || 
                    ThemeMode.TRANSPARENT_LIGHT.equals(mode);

            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());

            controller.setAppearanceLightStatusBars(isLightIconsNeeded);
            controller.setAppearanceLightNavigationBars(isLightIconsNeeded);
        }
    }
}
