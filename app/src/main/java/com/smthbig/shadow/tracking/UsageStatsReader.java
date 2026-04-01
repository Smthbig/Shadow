package com.smthbig.shadow.tracking;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public final class UsageStatsReader {

    private final UsageStatsManager usageStatsManager;

    public UsageStatsReader(Context context) {
        this.usageStatsManager =
                (UsageStatsManager)
                        context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    /**
     * Returns total foreground time (ms) for a package since start of today.
     */
    public long getTodayForegroundTimeMs(String packageName) {
        if (usageStatsManager == null || packageName == null) {
            return 0L;
        }

        long start = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events =
                usageStatsManager.queryEvents(start, now);

        if (events == null) return 0L;

        UsageEvents.Event event = new UsageEvents.Event();

        long total = 0L;
        Long lastResume = null;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            if (!packageName.equals(event.getPackageName())) continue;

            int type = event.getEventType();

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastResume = event.getTimeStamp();
            }

            if (type == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (lastResume != null) {
                    long delta = event.getTimeStamp() - lastResume;
                    if (delta > 0) total += delta;
                    lastResume = null;
                }
            }
        }

        // App still active
        if (lastResume != null) {
            long delta = now - lastResume;
            if (delta > 0) total += delta;
        }

        return Math.max(0L, total);
    }

    /**
     * Returns package -> foreground time (ms) for today.
     */
    public Map<String, Long> getAllTodayForegroundTimes() {
        Map<String, Long> result = new HashMap<>();

        if (usageStatsManager == null) return result;

        long start = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events =
                usageStatsManager.queryEvents(start, now);

        if (events == null) return result;

        UsageEvents.Event event = new UsageEvents.Event();

        Map<String, Long> active = new HashMap<>();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            String pkg = event.getPackageName();
            int type = event.getEventType();

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                active.put(pkg, event.getTimeStamp());
            }

            if (type == UsageEvents.Event.ACTIVITY_PAUSED) {
                Long startTime = active.remove(pkg);
                if (startTime != null) {
                    long delta = event.getTimeStamp() - startTime;
                    if (delta > 0) {
                        result.put(
                                pkg,
                                result.getOrDefault(pkg, 0L) + delta
                        );
                    }
                }
            }
        }

        // Still running apps
        for (Map.Entry<String, Long> entry : active.entrySet()) {
            long delta = now - entry.getValue();
            if (delta > 0) {
                result.put(
                        entry.getKey(),
                        result.getOrDefault(entry.getKey(), 0L) + delta
                );
            }
        }

        return result;
    }

    private long startOfTodayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}