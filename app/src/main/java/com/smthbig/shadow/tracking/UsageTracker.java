package com.smthbig.shadow.tracking;

import android.content.Context;
import android.content.SharedPreferences;

import com.smthbig.shadow.repository.UsageStatsRepository;

public final class UsageTracker {

    private static final String PREF_STATS = "shadow_friction_stats";
    private final UsageStatsRepository usageStatsRepository;
    private final SharedPreferences statsPrefs;

    public UsageTracker(Context context, UsageStatsRepository repository) {
        this.usageStatsRepository = repository;
        this.statsPrefs = context.getSharedPreferences(PREF_STATS, Context.MODE_PRIVATE);
    }

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

    public long getTodayUsageMs(String packageName) {
        return usageStatsRepository.getTodayUsageMs(packageName);
    }

    public UsageSnapshot getSnapshot(String packageName) {
        long usedMs = getTodayUsageMs(packageName);
        return new UsageSnapshot(packageName, usedMs);
    }
}
