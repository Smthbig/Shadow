package com.smthbig.shadow.repository;

import android.content.Context;
import android.util.Log;

import com.smthbig.shadow.tracking.UsageStatsReader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class UsageStatsRepository {

    private static final String TAG = "UsageStatsRepo";
    private static final long CACHE_TTL_MS = TimeUnit.SECONDS.toMillis(30);

    private final UsageStatsReader reader;
    private final ConcurrentHashMap<String, CachedUsage> cache = new ConcurrentHashMap<>();

    public UsageStatsRepository(Context context) {
        this.reader = new UsageStatsReader(context);
    }

    public long getTodayUsageMs(String packageName) {
        CachedUsage cached = cache.get(packageName);
        long now = System.currentTimeMillis();

        if (cached != null && (now - cached.timestamp) < CACHE_TTL_MS) {
            return cached.usageMs;
        }

        long fresh = reader.getTodayForegroundTimeMs(packageName);
        cache.put(packageName, new CachedUsage(fresh, now));
        return fresh;
    }

    public Map<String, Long> getAllTodayUsage() {
        long now = System.currentTimeMillis();
        Map<String, Long> fresh = reader.getAllTodayForegroundTimes();

        for (Map.Entry<String, Long> entry : fresh.entrySet()) {
            cache.put(entry.getKey(), new CachedUsage(entry.getValue(), now));
        }

        return fresh;
    }

    public String getForegroundPackage() {
        return reader.getForegroundPackage();
    }

    public void invalidate(String packageName) {
        cache.remove(packageName);
    }

    public void invalidateAll() {
        cache.clear();
    }

    private static final class CachedUsage {
        final long usageMs;
        final long timestamp;

        CachedUsage(long usageMs, long timestamp) {
            this.usageMs = usageMs;
            this.timestamp = timestamp;
        }
    }
}
