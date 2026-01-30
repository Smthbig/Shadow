package com.smthbig.shadow.tracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import java.util.concurrent.TimeUnit;

/**
 * Event-driven usage tracker.
 * Stores only aggregated time. No background execution.
 */
public final class UsageTrackerImpl implements UsageTracker {

    private static final String PREF = "usage_tracker_v1";

    private static final String KEY_DAY = "day";
    private static final String KEY_ACTIVE_PKG = "active_pkg";
    private static final String KEY_ACTIVE_START = "active_start";

    private static final String BASE_USED = "base_used_";
    private static final String EXT_GRANTED = "ext_granted_";
    private static final String EXT_USED = "ext_used_";
    private static final String EXT_COUNT = "ext_count_";

    private static final long DEFAULT_DAILY_LIMIT_MS =
            TimeUnit.HOURS.toMillis(2);

    private final SharedPreferences prefs;

    public UsageTrackerImpl(Context context) {
        this.prefs =
                context
                        .getApplicationContext()
                        .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    /* ---------------- lifecycle ---------------- */

    @Override
    public synchronized void onAppLaunchStart(
            String packageName,
            long timestampMs
    ) {
        ensureDayBoundary(timestampMs);

        closeActiveSession(timestampMs);

        prefs.edit()
                .putString(KEY_ACTIVE_PKG, packageName)
                .putLong(KEY_ACTIVE_START, timestampMs)
                .apply();
    }

    @Override
    public synchronized void onAppSessionEnd(long timestampMs) {
        ensureDayBoundary(timestampMs);
        closeActiveSession(timestampMs);
    }

    /* ---------------- limits ---------------- */

    @Override
    public long getDailyBaseLimitMs(String packageName) {
        return DEFAULT_DAILY_LIMIT_MS;
    }

    @Override
    public long getBaseUsedTodayMs(String packageName) {
        return prefs.getLong(BASE_USED + packageName, 0L);
    }

    /* ---------------- extensions ---------------- */

    @Override
    public long getExtensionGrantedTodayMs(String packageName) {
        return prefs.getLong(EXT_GRANTED + packageName, 0L);
    }

    @Override
    public long getExtensionUsedTodayMs(String packageName) {
        return prefs.getLong(EXT_USED + packageName, 0L);
    }

    @Override
    public int getExtensionCountToday(String packageName) {
        return prefs.getInt(EXT_COUNT + packageName, 0);
    }

    @Override
    public synchronized void grantExtension(
            String packageName,
            long extensionMs,
            long timestampMs
    ) {
        ensureDayBoundary(timestampMs);

        prefs.edit()
                .putLong(
                        EXT_GRANTED + packageName,
                        getExtensionGrantedTodayMs(packageName) + extensionMs
                )
                .putInt(
                        EXT_COUNT + packageName,
                        getExtensionCountToday(packageName) + 1
                )
                .apply();
    }

    /* ---------------- policy helpers ---------------- */

    @Override
    public long getRemainingAllowedMs(String packageName) {
        long allowed =
                getDailyBaseLimitMs(packageName)
                        + getExtensionGrantedTodayMs(packageName);

        long used =
                getBaseUsedTodayMs(packageName)
                        + getExtensionUsedTodayMs(packageName);

        return allowed - used;
    }

    @Override
    public boolean isBlocked(String packageName) {
        return getRemainingAllowedMs(packageName) <= 0;
    }

    @Override
    public String getReason(String packageName) {
        if (!isBlocked(packageName)) {
            return "Usage delay applied";
        }
        return "Daily usage limit reached";
    }

    /* ---------------- day boundary ---------------- */

    @Override
    public synchronized void ensureDayBoundary(long timestampMs) {
        long today = timestampMs / DateUtils.DAY_IN_MILLIS;
        long stored = prefs.getLong(KEY_DAY, -1);

        if (stored == today) return;

        prefs.edit().clear()
                .putLong(KEY_DAY, today)
                .apply();
    }

    /* ---------------- internal ---------------- */

    private void closeActiveSession(long timestampMs) {
        String pkg = prefs.getString(KEY_ACTIVE_PKG, null);
        long start = prefs.getLong(KEY_ACTIVE_START, -1);

        if (pkg == null || start <= 0) return;

        long duration = Math.max(0, timestampMs - start);

        long baseUsed = getBaseUsedTodayMs(pkg);
        long baseLimit = getDailyBaseLimitMs(pkg);

        long baseRemaining = Math.max(0, baseLimit - baseUsed);
        long baseConsume = Math.min(baseRemaining, duration);
        long extraConsume = duration - baseConsume;

        prefs.edit()
                .putLong(BASE_USED + pkg, baseUsed + baseConsume)
                .putLong(
                        EXT_USED + pkg,
                        getExtensionUsedTodayMs(pkg) + Math.max(0, extraConsume)
                )
                .remove(KEY_ACTIVE_PKG)
                .remove(KEY_ACTIVE_START)
                .apply();
    }
}