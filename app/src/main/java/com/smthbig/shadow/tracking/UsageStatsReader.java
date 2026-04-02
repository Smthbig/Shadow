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

    /* ========================================================= */
    /* ================= SINGLE APP USAGE ======================= */
    /* ========================================================= */

    public long getTodayForegroundTimeMs(String packageName) {

        if (usageStatsManager == null || packageName == null) {
            return 0L;
        }

        long start = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(start, now);
        if (events == null) return 0L;

        UsageEvents.Event event = new UsageEvents.Event();

        long total = 0L;
        Long activeStart = null;

        while (events.hasNextEvent()) {

            events.getNextEvent(event);

            if (!packageName.equals(event.getPackageName())) continue;

            int type = event.getEventType();
            long time = event.getTimeStamp();

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                activeStart = time;
            }

            else if (type == UsageEvents.Event.ACTIVITY_PAUSED
                  || type == UsageEvents.Event.ACTIVITY_STOPPED) {

                if (activeStart != null) {
                    long delta = time - activeStart;

                    if (delta > 0 && delta < 1000L * 60 * 60 * 12) { // sanity cap (12h)
                        total += delta;
                    }

                    activeStart = null;
                }
            }
        }

        // still active
        if (activeStart != null) {
            long delta = now - activeStart;
            if (delta > 0) total += delta;
        }

        return Math.max(0L, total);
    }

    /* ========================================================= */
    /* ================= ALL APPS USAGE ========================= */
    /* ========================================================= */

    public Map<String, Long> getAllTodayForegroundTimes() {

        Map<String, Long> result = new HashMap<>();

        if (usageStatsManager == null) return result;

        long start = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(start, now);
        if (events == null) return result;

        UsageEvents.Event event = new UsageEvents.Event();

        Map<String, Long> active = new HashMap<>();

        while (events.hasNextEvent()) {

            events.getNextEvent(event);

            String pkg = event.getPackageName();
            int type = event.getEventType();
            long time = event.getTimeStamp();

            if (pkg == null) continue;

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                active.put(pkg, time);
            }

            else if (type == UsageEvents.Event.ACTIVITY_PAUSED
                  || type == UsageEvents.Event.ACTIVITY_STOPPED) {

                Long startTime = active.remove(pkg);

                if (startTime != null) {

                    long delta = time - startTime;

                    if (delta > 0 && delta < 1000L * 60 * 60 * 12) {
                        result.put(
                                pkg,
                                result.getOrDefault(pkg, 0L) + delta
                        );
                    }
                }
            }
        }

        // still running apps
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

    /* ========================================================= */
    /* ================= TIME BASE ============================== */
    /* ========================================================= */

    private long startOfTodayMillis() {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }
}