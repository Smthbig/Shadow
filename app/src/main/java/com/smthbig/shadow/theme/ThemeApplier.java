package com.smthbig.shadow.theme;

import android.app.Activity;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

final class ThemeApplier {

    private ThemeApplier() {}

    static void apply(Activity activity, String mode) {

        // 1. Apply night mode FIRST (critical)
        AppCompatDelegate.setDefaultNightMode(
                ThemeConfig.getNightMode(mode)
        );

        // 2. Apply theme
        activity.setTheme(
                ThemeConfig.getThemeRes(mode)
        );

        // 3. Apply dynamic colors LAST
        if (ThemeMode.DYNAMIC.equals(mode)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            DynamicColors.applyToActivityIfAvailable(activity);
        }
    }
}