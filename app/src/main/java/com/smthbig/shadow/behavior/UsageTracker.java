package com.smthbig.shadow.behavior;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Tracks per-app daily usage with base limit + extensions.
 * No background services. Launcher-driven accounting.
 */
public final class UsageTracker {

    private static final String PREFS = "usage_tracker_v2";

    private static final long DEFAULT_BASE_LIMIT_MS = 60 * 60 * 1000L; // 1 hour
    private static final long EXTENSION_UNIT_MS = 5 * 60 * 1000L;      // 5 min

    private final SharedPreferences prefs;

    public UsageTracker(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /* ───────── Public API used by Launcher ───────── */

    public long getDailyBaseLimitMs(String pkg) {
        resetIfNewDay(pkg);
        return prefs.getLong(key(pkg, "base_limit"), DEFAULT_BASE_LIMIT_MS);
    }

    public long getUsedBaseMsToday(String pkg) {
        resetIfNewDay(pkg);
        return prefs.getLong(key(pkg, "base_used"), 0L);
    }

    public long getExtensionGrantedMs(String pkg) {
        resetIfNewDay(pkg);
        return prefs.getLong(key(pkg, "ext_granted"), 0L);
    }

    public long getUsedExtensionMs(String pkg) {
        resetIfNewDay(pkg);
        return prefs.getLong(key(pkg, "ext_used"), 0L);
    }

    public int getExtensionCountToday(String pkg) {
        resetIfNewDay(pkg);
        return prefs.getInt(key(pkg, "ext_count"), 0);
    }

    /**
     * Call when launcher decides to allow app start.
     * Time accounting is coarse on purpose.
     */
    public void onLaunchStarted(String pkg) {
        resetIfNewDay(pkg);

        long now = System.currentTimeMillis();
        prefs.edit()
                .putLong(key(pkg, "last_launch"), now)
                .apply();
    }

    /**
     * Grant user an extra 5 min block.
     */
    public void grantExtension(String pkg) {
        resetIfNewDay(pkg);

        prefs.edit()
                .putLong(
                        key(pkg, "ext_granted"),
                        getExtensionGrantedMs(pkg) + EXTENSION_UNIT_MS
                )
                .putInt(
                        key(pkg, "ext_count"),
                        getExtensionCountToday(pkg) + 1
                )
                .apply();
    }

    /* ───────── Internal helpers ───────── */

    private void resetIfNewDay(String pkg) {
        long lastDay = prefs.getLong(key(pkg, "day"), -1);
        long today = todayKey();

        if (lastDay == today) return;

        prefs.edit()
                .putLong(key(pkg, "day"), today)
                .putLong(key(pkg, "base_used"), 0L)
                .putLong(key(pkg, "ext_used"), 0L)
                .putLong(key(pkg, "ext_granted"), 0L)
                .putInt(key(pkg, "ext_count"), 0)
                .remove(key(pkg, "last_launch"))
                .apply();
    }

    private static long todayKey() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static String key(String pkg, String suffix) {
        return pkg + "_" + suffix;
    }
}