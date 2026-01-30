package com.smthbig.shadow.tracking;

/**
 * Launcher-scoped usage ledger.
 * Event-driven. No background execution.
 */
public interface UsageTracker {

    /* ---------- Launch lifecycle ---------- */

    /**
     * Call when an app is about to be launched from the launcher.
     * Must be idempotent.
     */
    void onAppLaunchStart(String packageName, long timestampMs);

    /**
     * Call when user returns to launcher or another app is launched.
     * Safely closes the previous session.
     */
    void onAppSessionEnd(long timestampMs);

    /* ---------- Daily base limit ---------- */

    /**
     * Fixed daily limit in milliseconds.
     * Example: 2 hours = 2 * 60 * 60 * 1000
     */
    long getDailyBaseLimitMs(String packageName);

    /**
     * Total base time used today (excluding extensions).
     */
    long getBaseUsedTodayMs(String packageName);

    /* ---------- Extension handling ---------- */

    /**
     * Total extension time granted today.
     */
    long getExtensionGrantedTodayMs(String packageName);

    /**
     * Total extension time already consumed today.
     */
    long getExtensionUsedTodayMs(String packageName);

    /**
     * How many extensions user has taken today.
     */
    int getExtensionCountToday(String packageName);

    /**
     * Grant a one-time extension (e.g. +5 or +10 minutes).
     */
    void grantExtension(
            String packageName,
            long extensionMs,
            long timestampMs
    );

    /* ---------- Policy helpers ---------- */

    /**
     * Returns remaining allowed time (base + extensions - used).
     * Can be negative.
     */
    long getRemainingAllowedMs(String packageName);

    /**
     * True if app should be hard-blocked right now.
     */
    boolean isBlocked(String packageName);

    /**
     * Human-readable explanation for block or delay.
     * Never null.
     */
    String getReason(String packageName);

    /* ---------- Day rollover ---------- */

    /**
     * Must be called opportunistically (on launch).
     * Resets daily counters if date changed.
     */
    void ensureDayBoundary(long timestampMs);
}