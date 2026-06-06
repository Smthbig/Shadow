package com.smthbig.shadow.theme;

import android.app.Activity;
import android.content.SharedPreferences;

public final class ThemeRestarter {

    private static final String PREF = "shadow_theme_restart";
    private static final String KEY_PENDING_RESTART = "pending_restart";

    private ThemeRestarter() {}

    public static void restart(Activity activity) {
        setPendingRestart(activity, true);
        activity.recreate();
    }

    public static boolean consumePendingRestart(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
        boolean pending = prefs.getBoolean(KEY_PENDING_RESTART, false);
        if (pending) {
            prefs.edit().remove(KEY_PENDING_RESTART).apply();
        }
        return pending;
    }

    private static void setPendingRestart(Activity activity, boolean pending) {
        activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_PENDING_RESTART, pending)
                .apply();
    }
}