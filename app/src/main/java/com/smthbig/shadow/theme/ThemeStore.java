package com.smthbig.shadow.theme;

import android.content.Context;
import android.content.SharedPreferences;

public final class ThemeStore {

    private static final String PREF = "shadow_theme_store";
    private static final String KEY_MODE = "theme_mode";
    private static final String KEY_BG = "bg_type";

    private ThemeStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /* ---------- THEME ---------- */

    public static void setTheme(Context context, String mode) {
        prefs(context).edit().putString(KEY_MODE, mode).apply();
    }

    public static String getTheme(Context context) {
        return prefs(context).getString(KEY_MODE, ThemeMode.SYSTEM);
    }

    /* ---------- BACKGROUND ---------- */

    public static void setBackground(Context context, String type) {
        prefs(context).edit().putString(KEY_BG, type).apply();
    }

    public static String getBackground(Context context) {
        // Default to 'default' shadow gradient
        return prefs(context).getString(KEY_BG, "default");
    }
}
