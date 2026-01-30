package com.smthbig.shadow.extension;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

public final class ExtensionEngine {

    private static final String PREFS = "shadow_extensions";

    private static final String KEY_DAY = "day";
    private static final String KEY_GRANTED_MS = "granted_ms";
    private static final String KEY_USED_MS = "used_ms";

    private static final long MAX_DAILY_EXTENSION_MS =
            TimeUnit.MINUTES.toMillis(30);

    private final SharedPreferences prefs;

    public ExtensionEngine(Context context) {
        this.prefs =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFS,
                                Context.MODE_PRIVATE
                        );
        resetIfNewDay();
    }

    /* ---------- Public API ---------- */

    public synchronized boolean canGrant(long ms) {
        resetIfNewDay();
        return getGrantedMs() + ms <= MAX_DAILY_EXTENSION_MS;
    }

    public synchronized boolean grant(long ms) {
        resetIfNewDay();

        long granted = getGrantedMs();
        if (granted + ms > MAX_DAILY_EXTENSION_MS) {
            return false;
        }

        prefs.edit()
                .putLong(KEY_GRANTED_MS, granted + ms)
                .apply();

        return true;
    }

    /**
     * Remaining extension time available (ms)
     */
    public synchronized long getRemainingMs() {
        resetIfNewDay();
        return Math.max(
                0,
                getGrantedMs() - getUsedMs()
        );
    }

    /**
     * Consume extension time based on real foreground usage.
     * Must be called with deltaMs (not absolute).
     */
    public synchronized void consume(long deltaMs) {
        if (deltaMs <= 0) return;

        resetIfNewDay();

        long used = getUsedMs();
        long granted = getGrantedMs();

        long newUsed =
                Math.min(
                        granted,
                        used + deltaMs
                );

        if (newUsed != used) {
            prefs.edit()
                    .putLong(KEY_USED_MS, newUsed)
                    .apply();
        }
    }

    /* ---------- Internals ---------- */

    private void resetIfNewDay() {
        long today = todayKey();
        long storedDay = prefs.getLong(KEY_DAY, -1);

        if (storedDay != today) {
            prefs.edit()
                    .putLong(KEY_DAY, today)
                    .putLong(KEY_GRANTED_MS, 0)
                    .putLong(KEY_USED_MS, 0)
                    .apply();
        }
    }

    private long getGrantedMs() {
        return prefs.getLong(KEY_GRANTED_MS, 0);
    }

    private long getUsedMs() {
        return prefs.getLong(KEY_USED_MS, 0);
    }

    /**
     * Day key aligned to local midnight
     */
    private long todayKey() {
        return System.currentTimeMillis()
                / TimeUnit.DAYS.toMillis(1);
    }
}