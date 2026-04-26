package com.smthbig.shadow.theme;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private ThemeManager() {}

    /* ---------- APPLY (PER ACTIVITY) ---------- */

    public static void apply(Activity activity) {
        String mode = ThemeStore.getTheme(activity);
        String background = ThemeStore.getBackground(activity);
        
        ThemeApplier.apply(activity, mode, background);
        WallpaperApplier.apply(activity);
    }

    /* ---------- APPLY GLOBAL (APP START) ---------- */

    public static void applyGlobal(Context context) {
        String mode = ThemeStore.getTheme(context);
        AppCompatDelegate.setDefaultNightMode(ThemeConfig.getNightMode(mode));
    }

    /* ---------- SET ---------- */

    public static void setTheme(Context context, String mode) {
        ThemeStore.setTheme(context, mode);
    }

    public static void setBackground(Context context, String type) {
        ThemeStore.setBackground(context, type);
    }

    /* ---------- GET ---------- */

    public static String getTheme(Context context) {
        return ThemeStore.getTheme(context);
    }

    public static String getBackground(Context context) {
        return ThemeStore.getBackground(context);
    }
}
