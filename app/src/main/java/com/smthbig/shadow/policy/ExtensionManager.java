package com.smthbig.shadow.policy;

import android.content.Context;
import android.content.SharedPreferences;

public final class ExtensionManager {

    private static final String PREF = "shadow_extensions";
    private static final long EXTENSION_UNIT_MS = 5 * 60 * 1000L; // 5 min
    private static final int MAX_EXTENSIONS_PER_DAY = 3;

    private ExtensionManager() {}

    public static boolean canGrantExtension(
            Context context,
            String pkg,
            int todayKey
    ) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        int count = sp.getInt(key(pkg, todayKey, "count"), 0);
        return count < MAX_EXTENSIONS_PER_DAY;
    }

    public static void grantExtension(
            Context context,
            String pkg,
            int todayKey
    ) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        String countKey = key(pkg, todayKey, "count");
        String grantKey = key(pkg, todayKey, "granted");

        int count = sp.getInt(countKey, 0);
        long granted = sp.getLong(grantKey, 0);

        sp.edit()
                .putInt(countKey, count + 1)
                .putLong(grantKey, granted + EXTENSION_UNIT_MS)
                .apply();
    }

    public static long getGrantedMs(
            Context context,
            String pkg,
            int todayKey
    ) {
        return context
                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getLong(key(pkg, todayKey, "granted"), 0);
    }

    public static int getExtensionCount(
            Context context,
            String pkg,
            int todayKey
    ) {
        return context
                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getInt(key(pkg, todayKey, "count"), 0);
    }

    private static String key(String pkg, int day, String suffix) {
        return pkg + "_" + day + "_" + suffix;
    }
}