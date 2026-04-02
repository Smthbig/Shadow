package com.smthbig.shadow.theme;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private ThemeManager() {}

    /* ---------- APPLY (PER ACTIVITY) ---------- */

    public static void apply(Activity activity) {
        String mode = get(activity);
        ThemeApplier.apply(activity, mode);
    }

    /* ---------- APPLY GLOBAL (APP START) ---------- */

    public static void applyGlobal(Context context) {
        String mode = get(context);

        AppCompatDelegate.setDefaultNightMode(
                ThemeConfig.getNightMode(mode)
        );
    }

    /* ---------- SET ---------- */

    public static void set(Context context, String mode) {
        ThemeStore.set(context, mode);
    }

    /* ---------- GET ---------- */

    public static String get(Context context) {
        return ThemeStore.get(context);
    }
}