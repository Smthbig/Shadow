package com.smthbig.shadow.theme;

import android.content.Context;
import android.content.SharedPreferences;

final class ThemeStore {

    private static final String PREF = "theme_pref";
    private static final String KEY = "mode";

    private ThemeStore() {}

    /* ---------- SET ---------- */

    static void set(Context context, String mode) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        // 🔥 Use commit() for immediate persistence (important for restart)
        prefs.edit()
                .putString(KEY, mode)
                .commit();
    }

    /* ---------- GET ---------- */

    static String get(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        String mode = prefs.getString(KEY, ThemeMode.SYSTEM);

        // safety fallback
        if (mode == null) {
            return ThemeMode.SYSTEM;
        }

        return mode;
    }
}