package com.smthbig.shadow.tracking;

import android.content.Context;

public final class UsageTracker {

    private final UsageStatsReader reader;

    public UsageTracker(Context context) {
        this.reader = new UsageStatsReader(context);
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