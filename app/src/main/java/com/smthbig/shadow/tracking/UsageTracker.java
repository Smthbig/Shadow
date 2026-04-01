package com.smthbig.shadow.tracking;

import android.content.Context;

import com.smthbig.shadow.extension.ExtensionEngine;

public final class UsageTracker {

    private final UsageStatsReader reader;
    private final ExtensionEngine extensionEngine;

    public UsageTracker(Context context) {
        this.reader = new UsageStatsReader(context);
        this.extensionEngine = new ExtensionEngine(context);
    }

    /* ---------- CORE ---------- */

    public long getTodayUsageMs(String packageName) {
        long usage = reader.getTodayForegroundTimeMs(packageName);

        // 🔥 CRITICAL: consume extension based on usage delta
        extensionEngine.consume(packageName, usage);

        return usage;
    }

    public long getTodayUsageMinutes(String packageName) {
        return getTodayUsageMs(packageName) / (60 * 1000);
    }

    /* ---------- EXTENSION ---------- */

    public long getRemainingExtensionMs(String packageName) {
        return extensionEngine.getRemainingMs(packageName);
    }
}