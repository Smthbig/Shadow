package com.smthbig.shadow.setup;

import android.content.Context;
import android.content.SharedPreferences;

public class SetupManager {

    private static final String PREF = "shadow_setup";
    private static final String KEY_DONE = "setup_done";

    public static boolean isSetupDone(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_DONE, false);
    }

    public static void markSetupDone(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DONE, true)
                .apply();
    }
}