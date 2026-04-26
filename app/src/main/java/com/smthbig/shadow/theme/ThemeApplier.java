package com.smthbig.shadow.theme;

import android.app.Activity;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.color.DynamicColors;

final class ThemeApplier {

    private ThemeApplier() {}

    static void apply(Activity activity, String mode) {

        int targetNightMode = ThemeConfig.getNightMode(mode);

        // 1. Apply night mode ONLY if changed (prevents double recreation)
        if (AppCompatDelegate.getDefaultNightMode() != targetNightMode) {
            AppCompatDelegate.setDefaultNightMode(targetNightMode);
        }

        // 2. Apply theme
        activity.setTheme(
                ThemeConfig.getThemeRes(mode)
        );

        // 3. System UI
        applySystemUi(activity, mode);

        // 4. Apply dynamic colors LAST
        if (ThemeMode.DYNAMIC.equals(mode)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            DynamicColors.applyToActivityIfAvailable(activity);
        }
    }

    private static void applySystemUi(Activity activity, String mode) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            android.view.Window window = activity.getWindow();

            //  Transparent bars
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

            //  Edge-to-edge
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false);

            //  Icons appearance
            boolean isLight = ThemeMode.LIGHT.equals(mode);

            androidx.core.view.WindowInsetsControllerCompat controller =
                    new androidx.core.view.WindowInsetsControllerCompat(window, window.getDecorView());

            controller.setAppearanceLightStatusBars(isLight);
            controller.setAppearanceLightNavigationBars(isLight);
        }
    }
}