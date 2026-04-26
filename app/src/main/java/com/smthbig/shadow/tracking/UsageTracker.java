package com.smthbig.shadow.tracking;

import android.content.Context;
import android.content.SharedPreferences;

public final class UsageTracker {

    private static final String PREF_STATS = "shadow_friction_stats";
    private final UsageStatsReader reader;
    private final SharedPreferences statsPrefs;

    public UsageTracker(Context context) {
        this.reader = new UsageStatsReader(context);
        this.statsPrefs = context.getSharedPreferences(PREF_STATS, Context.MODE_PRIVATE);
    }

    /* ---------- FRICTION LOGGING ---------- */

    public void logDelay() {
        increment("total_delays");
    }

    public void logBlock() {
        increment("total_blocks");
    }

    private void increment(String key) {
        int val = statsPrefs.getInt(key, 0);
        statsPrefs.edit().putInt(key, val + 1).apply();
    }

    public int getTotalDelays() {
        return statsPrefs.getInt("total_delays", 0);
    }

    public int getTotalBlocks() {
        return statsPrefs.getInt("total_blocks", 0);
    }

    /* ---------- CORE ---------- */

    public long getTodayUsageMs(String packageName) {
        return reader.getTodayForegroundTimeMs(packageName);
    }

    public long getTodayUsageMinutes(String packageName) {
        return getTodayUsageMs(packageName) / (60 * 1000);
    }

    /* ---------- STATE SNAPSHOT (IMPORTANT) ---------- */

    public UsageSnapshot getSnapshot(String packageName) {

        long usedMs = getTodayUsageMs(packageName);

        return new UsageSnapshot(
                packageName,
                usedMs
        );
    }
}