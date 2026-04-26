package com.smthbig.shadow.tracking;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class UsageStatsReader {

    private final UsageStatsManager usageStatsManager;

    public UsageStatsReader(Context context) {
        this.usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    /* ========================================================= */
    /* ================= FOREGROUND ============================ */
    /* ========================================================= */

    public String getForegroundPackage() {

        if (usageStatsManager == null) return null;

        long now = System.currentTimeMillis();
        long start = now - TimeUnit.MINUTES.toMillis(1); // look at last 1 min

        try {
            UsageEvents events = usageStatsManager.queryEvents(start, now);
            if (events == null || !events.hasNextEvent()) return null;

            UsageEvents.Event event = new UsageEvents.Event();
            String lastPkg = null;

            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    lastPkg = event.getPackageName();
                }
            }

            return lastPkg;
        } catch (Exception e) {
            return null; // Permission revoked or API failure
        }
    }

    /* ========================================================= */
    /* ================= SINGLE APP ============================ */
    /* ========================================================= */

    public long getTodayForegroundTimeMs(String packageName) {

        if (usageStatsManager == null || packageName == null) return 0L;

        long start = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(start, now);
        if (events == null) return 0L;

        UsageEvents.Event event = new UsageEvents.Event();

        long total = 0L;
        Long activeStart = null;
        String currentForeground = null;

        while (events.hasNextEvent()) {

            events.getNextEvent(event);

            String pkg = event.getPackageName();
            int type = event.getEventType();
            long time = event.getTimeStamp();

            if (pkg == null) continue;

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {

                // close previous foreground if switching
                if (currentForeground != null
                        && activeStart != null
                        && currentForeground.equals(packageName)) {

                    long delta = time - activeStart;
                    if (isValidDelta(delta)) total += delta;
                }

                currentForeground = pkg;

                if (pkg.equals(packageName)) {
                    activeStart = time;
                } else {
                    activeStart = null;
                }
            }

            else if (type == UsageEvents.Event.ACTIVITY_PAUSED
                  || type == UsageEvents.Event.ACTIVITY_STOPPED) {

                if (pkg.equals(packageName) && activeStart != null) {

                    long delta = time - activeStart;
                    if (isValidDelta(delta)) total += delta;

                    activeStart = null;
                }
            }
        }

        // ongoing session
        if (activeStart != null && packageName.equals(currentForeground)) {
            long delta = now - activeStart;
            if (isValidDelta(delta)) total += delta;
        }

        return Math.max(total, 0L);
    }

    /* ========================================================= */
    /* ================= ALL APPS ============================== */
    /* ========================================================= */

    public Map<String, Long> getAllTodayForegroundTimes() {

        Map<String, Long> result = new HashMap<>();

        if (usageStatsManager == null) return result;

        long start = startOfTodayMillis();
        long now = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(start, now);
        if (events == null) return result;

        UsageEvents.Event event = new UsageEvents.Event();

        String currentForeground = null;
        long activeStart = 0L;

        while (events.hasNextEvent()) {

            events.getNextEvent(event);

            String pkg = event.getPackageName();
            int type = event.getEventType();
            long time = event.getTimeStamp();

            if (pkg == null) continue;

            if (type == UsageEvents.Event.ACTIVITY_RESUMED) {

                // close previous app
                if (currentForeground != null) {
                    long delta = time - activeStart;
                    if (isValidDelta(delta)) {
                        result.put(
                                currentForeground,
                                result.getOrDefault(currentForeground, 0L) + delta
                        );
                    }
                }

                currentForeground = pkg;
                activeStart = time;
            }

            else if (type == UsageEvents.Event.ACTIVITY_PAUSED
                  || type == UsageEvents.Event.ACTIVITY_STOPPED) {

                if (pkg.equals(currentForeground)) {

                    long delta = time - activeStart;
                    if (isValidDelta(delta)) {
                        result.put(
                                pkg,
                                result.getOrDefault(pkg, 0L) + delta
                        );
                    }

                    currentForeground = null;
                }
            }
        }

        // still active app
        if (currentForeground != null) {
            long delta = now - activeStart;
            if (isValidDelta(delta)) {
                result.put(
                        currentForeground,
                        result.getOrDefault(currentForeground, 0L) + delta
                );
            }
        }

        return result;
    }

    /* ========================================================= */
    /* ================= HELPERS =============================== */
    /* ========================================================= */

    private boolean isValidDelta(long delta) {
        return delta > 0 && delta < 1000L * 60 * 60 * 12;
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