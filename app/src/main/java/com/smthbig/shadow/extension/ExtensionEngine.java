package com.smthbig.shadow.extension;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class ExtensionEngine {

    private static final String PREF = "extension_store";

    /* ---------- LIMITS ---------- */

    private static final long MAX_EXTENSION_PER_DAY =
            TimeUnit.MINUTES.toMillis(20);

    private static final long MAX_SINGLE_GRANT =
            TimeUnit.MINUTES.toMillis(10);

    private static final long COOLDOWN_MS =
            TimeUnit.MINUTES.toMillis(2);

    private final SharedPreferences prefs;

    public ExtensionEngine(Context context) {
        this.prefs =
                context.getApplicationContext()
                        .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /* ========================================================= */
    /* ===================== GRANT ============================== */
    /* ========================================================= */

    public boolean grant(String pkg, long requestMs) {

        if (pkg == null || requestMs <= 0) return false;

        long now = System.currentTimeMillis();

        resetIfNewDay(pkg);

        long lastGrant = getLong(pkg, "lastGrant");
        long dailyUsed = getLong(pkg, "daily");

        // ❌ cooldown
        if (now - lastGrant < COOLDOWN_MS) {
            return false;
        }

        // ❌ cap single
        long durationMs = Math.min(requestMs, MAX_SINGLE_GRANT);

        // ❌ cap daily
        if (dailyUsed >= MAX_EXTENSION_PER_DAY) {
            return false;
        }

        long allowed =
                Math.min(durationMs, MAX_EXTENSION_PER_DAY - dailyUsed);

        if (allowed <= 0) return false;

        long granted = getGranted(pkg);

        prefs.edit()
                .putLong(key(pkg, "granted"), granted + allowed)
                .putLong(key(pkg, "daily"), dailyUsed + allowed)
                .putLong(key(pkg, "lastGrant"), now)
                .apply();

        return true;
    }

    /* ========================================================= */
    /* ===================== STATE ============================== */
    /* ========================================================= */

    /**
     * PURE FUNCTION:
     * Remaining extension = granted - consumed
     */
    public long getRemainingMs(
            String pkg,
            long usedMs,
            long baseLimitMs
    ) {

        if (pkg == null) return 0;

        resetIfNewDay(pkg);

        long granted = getGranted(pkg);
        if (granted <= 0) return 0;

        long consumed = getConsumedExtensionMs(usedMs, baseLimitMs);

        return Math.max(0, granted - consumed);
    }

    public boolean hasExtension(
            String pkg,
            long usedMs,
            long baseLimitMs
    ) {
        return getRemainingMs(pkg, usedMs, baseLimitMs) > 0;
    }

    /**
     * Derived consumption
     */
    public long getConsumedExtensionMs(
            long usedMs,
            long baseLimitMs
    ) {
        return Math.max(0, usedMs - baseLimitMs);
    }

    /* ========================================================= */
    /* ===================== METADATA =========================== */
    /* ========================================================= */

    public long getGrantedMs(String pkg) {
        return getGranted(pkg);
    }

    public long getDailyUsedMs(String pkg) {
        resetIfNewDay(pkg);
        return getLong(pkg, "daily");
    }

    public long getCooldownRemainingMs(String pkg) {

        long lastGrant = getLong(pkg, "lastGrant");
        long now = System.currentTimeMillis();

        long remaining = COOLDOWN_MS - (now - lastGrant);

        return Math.max(0, remaining);
    }

    public boolean canGrant(String pkg) {

        long now = System.currentTimeMillis();

        resetIfNewDay(pkg);

        long lastGrant = getLong(pkg, "lastGrant");
        long dailyUsed = getLong(pkg, "daily");

        if (now - lastGrant < COOLDOWN_MS) return false;
        if (dailyUsed >= MAX_EXTENSION_PER_DAY) return false;

        return true;
    }

    /* ========================================================= */
    /* ===================== RESET ============================== */
    /* ========================================================= */

    private void resetIfNewDay(String pkg) {

        int today = currentDay();
        int storedDay = (int) getLong(pkg, "day");

        if (storedDay != today) {
            prefs.edit()
                    .putLong(key(pkg, "daily"), 0)
                    .putLong(key(pkg, "granted"), 0)
                    .putLong(key(pkg, "day"), today)
                    .apply();
        }
    }

    /* ========================================================= */
    /* ===================== INTERNAL =========================== */
    /* ========================================================= */

    private long getGranted(String pkg) {
        return getLong(pkg, "granted");
    }

    private long getLong(String pkg, String field) {
        return prefs.getLong(key(pkg, field), 0L);
    }

    private String key(String pkg, String field) {
        return pkg + "_" + field;
    }

    private int currentDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_YEAR);
    }

    /* ========================================================= */
    /* ===================== CLEAR ============================== */
    /* ========================================================= */

    public void clear(String pkg) {
        prefs.edit()
                .remove(key(pkg, "granted"))
                .remove(key(pkg, "daily"))
                .remove(key(pkg, "lastGrant"))
                .remove(key(pkg, "day"))
                .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}