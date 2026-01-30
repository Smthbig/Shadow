package com.smthbig.shadow.tracking;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

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

        long startOfDay = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events =
                usageStatsManager.queryEvents(startOfDay, now);

        if (events == null) return 0L;

        long totalTime = 0L;
        Long lastForegroundStart = null;

        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);

            if (!packageName.equals(event.getPackageName())) {
                continue;
            }

            int type = event.getEventType();

            if (isMoveToForeground(type)) {
                lastForegroundStart = event.getTimeStamp();
            } else if (isMoveToBackground(type)) {
                if (lastForegroundStart != null) {
                    long delta =
                            event.getTimeStamp() - lastForegroundStart;
                    if (delta > 0) {
                        totalTime += delta;
                    }
                    lastForegroundStart = null;
                }
            }
        }

        // App still in foreground
        if (lastForegroundStart != null) {
            long delta = now - lastForegroundStart;
            if (delta > 0) {
                totalTime += delta;
            }
        }

        return totalTime;
    }

    /**
     * Returns package -> foreground time (ms) for today.
     */
    public Map<String, Long> getAllTodayForegroundTimes() {
        Map<String, Long> result = new HashMap<>();

        if (usageStatsManager == null) return result;

        long startOfDay = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events =
                usageStatsManager.queryEvents(startOfDay, now);

        if (events == null) return result;

        Map<String, Long> activeSessions = new HashMap<>();

        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);

            String pkg = event.getPackageName();
            int type = event.getEventType();

            if (isMoveToForeground(type)) {
                activeSessions.put(pkg, event.getTimeStamp());
            } else if (isMoveToBackground(type)) {
                Long start = activeSessions.remove(pkg);
                if (start != null) {
                    long delta = event.getTimeStamp() - start;
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
        for (Map.Entry<String, Long> entry
                : activeSessions.entrySet()) {
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

    /* ---------- Helpers ---------- */

    private boolean isMoveToForeground(int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return type == UsageEvents.Event.MOVE_TO_FOREGROUND;
        }
        return type == UsageEvents.Event.ACTIVITY_RESUMED;
    }

    private boolean isMoveToBackground(int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return type == UsageEvents.Event.MOVE_TO_BACKGROUND;
        }
        return type == UsageEvents.Event.ACTIVITY_PAUSED
                || type == UsageEvents.Event.ACTIVITY_STOPPED;
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