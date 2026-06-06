package com.smthbig.shadow.extension;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class ExtensionEngine {

    private static final String PREF = "extension_store";
    private static final long MAX_EXTENSION_PER_DAY_MS = TimeUnit.HOURS.toMillis(24);
    private static final long MAX_SINGLE_GRANT_MS = TimeUnit.MINUTES.toMillis(10);
    private static final long COOLDOWN_MS = 0;

    private final SharedPreferences prefs;

    public ExtensionEngine(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean grant(String pkg, long requestMs) {
        if (pkg == null || requestMs <= 0) return false;

        long now = System.currentTimeMillis();
        resetIfNewDay(pkg);

        long lastGrant = getLong(pkg, "lastGrant");
        long dailyUsed = getLong(pkg, "daily");

        if (now - lastGrant < COOLDOWN_MS) return false;
        if (dailyUsed >= MAX_EXTENSION_PER_DAY_MS) return false;

        long allowed = Math.min(requestMs, MAX_SINGLE_GRANT_MS);
        allowed = Math.min(allowed, MAX_EXTENSION_PER_DAY_MS - dailyUsed);

        if (allowed <= 0) return false;

        long granted = getGranted(pkg);

        prefs.edit()
                .putLong(key(pkg, "granted"), granted + allowed)
                .putLong(key(pkg, "daily"), dailyUsed + allowed)
                .putLong(key(pkg, "lastGrant"), now)
                .apply();

        return true;
    }

    public void consume(String pkg, long ms) {
        if (pkg == null || ms <= 0) return;

        resetIfNewDay(pkg);

        long consumed = getConsumed(pkg);
        long granted = getGranted(pkg);
        long newConsumed = Math.min(granted, consumed + ms);

        prefs.edit()
                .putLong(key(pkg, "consumed"), newConsumed)
                .apply();
    }

    public long getRemainingMs(String pkg) {
        if (pkg == null) return 0;
        resetIfNewDay(pkg);
        long granted = getGranted(pkg);
        long consumed = getConsumed(pkg);
        return Math.max(0, granted - consumed);
    }

    public boolean hasExtension(String pkg) {
        return getRemainingMs(pkg) > 0;
    }

    public long getGrantedMs(String pkg) {
        return getGranted(pkg);
    }

    public long getConsumedMs(String pkg) {
        return getConsumed(pkg);
    }

    public long getDailyUsedMs(String pkg) {
        resetIfNewDay(pkg);
        return getLong(pkg, "daily");
    }

    public boolean canGrant(String pkg) {
        long now = System.currentTimeMillis();
        resetIfNewDay(pkg);

        long lastGrant = getLong(pkg, "lastGrant");
        long dailyUsed = getLong(pkg, "daily");

        if (now - lastGrant < COOLDOWN_MS) return false;
        if (dailyUsed >= MAX_EXTENSION_PER_DAY_MS) return false;

        return true;
    }

    private void resetIfNewDay(String pkg) {
        int today = currentDay();
        int storedDay = (int) getLong(pkg, "day");

        if (storedDay != today) {
            prefs.edit()
                    .putLong(key(pkg, "daily"), 0)
                    .putLong(key(pkg, "granted"), 0)
                    .putLong(key(pkg, "consumed"), 0)
                    .putLong(key(pkg, "day"), today)
                    .apply();
        }
    }

    private long getGranted(String pkg) {
        return getLong(pkg, "granted");
    }

    private long getConsumed(String pkg) {
        return getLong(pkg, "consumed");
    }

    private long getLong(String pkg, String field) {
        return prefs.getLong(key(pkg, field), 0L);
    }

    private String key(String pkg, String field) {
        return pkg + "_" + field;
    }

    private int currentDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    }

    public void clear(String pkg) {
        prefs.edit()
                .remove(key(pkg, "granted"))
                .remove(key(pkg, "consumed"))
                .remove(key(pkg, "daily"))
                .remove(key(pkg, "lastGrant"))
                .remove(key(pkg, "day"))
                .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}