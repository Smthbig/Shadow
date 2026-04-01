package com.smthbig.shadow.extension;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

public final class ExtensionEngine {

    private static final String PREFS = "shadow_extensions";

    private static final String KEY_DAY = "day";

    private static final long MAX_DAILY_EXTENSION_MS = TimeUnit.MINUTES.toMillis(30);

    private final SharedPreferences prefs;

    public ExtensionEngine(Context context) {
        this.prefs =
                context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        resetIfNewDay();
    }

    /* ---------- Public API (PER APP) ---------- */
    public synchronized boolean canGrant(String pkg, long ms) {
        resetIfNewDay();

        long used = getUsedMs(pkg);

        return used + ms <= MAX_DAILY_EXTENSION_MS;
    }

    public synchronized boolean grant(String pkg, long ms) {
        resetIfNewDay();

        long used = getUsedMs(pkg);

        if (used + ms > MAX_DAILY_EXTENSION_MS) {
            return false;
        }

        long granted = getGrantedMs(pkg);

        prefs.edit().putLong(keyGranted(pkg), granted + ms).apply();

        return true;
    }

    public synchronized long getRemainingMs(String pkg) {
        resetIfNewDay();

        return Math.max(0, getGrantedMs(pkg) - getUsedMs(pkg));
    }

    public synchronized void consume(String pkg, long deltaMs) {
        if (deltaMs <= 0) return;

        resetIfNewDay();

        long used = getUsedMs(pkg);
        long granted = getGrantedMs(pkg);

        long newUsed = Math.min(granted, used + deltaMs);

        if (newUsed != used) {
            prefs.edit().putLong(keyUsed(pkg), newUsed).apply();
        }
    }

    /* ---------- Key Helpers ---------- */

    private String keyGranted(String pkg) {
        return "granted_" + pkg;
    }

    private String keyUsed(String pkg) {
        return "used_" + pkg;
    }

    /* ---------- Internals ---------- */

    private void resetIfNewDay() {
        long today = todayKey();
        long storedDay = prefs.getLong(KEY_DAY, -1);

        if (storedDay != today) {
            prefs.edit()
                    .putLong(KEY_DAY, today)
                    .clear() // clear all per-app data
                    .apply();
        }
    }

    private long getGrantedMs(String pkg) {
        return prefs.getLong(keyGranted(pkg), 0);
    }

    private long getUsedMs(String pkg) {
        return prefs.getLong(keyUsed(pkg), 0);
    }

    private long todayKey() {
        return System.currentTimeMillis() / TimeUnit.DAYS.toMillis(1);
    }
}
