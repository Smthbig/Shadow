package com.smthbig.shadow.policy;

import java.util.concurrent.TimeUnit;

public final class TimePolicyEngine {

    /* ========================================================= */
    /* ================= THRESHOLDS ============================= */
    /* ========================================================= */

    private static final long SOFT_WARNING_MS =
            TimeUnit.MINUTES.toMillis(5);

    private static final long HARD_WARNING_MS =
            TimeUnit.MINUTES.toMillis(2);

    private static final long CRITICAL_MS =
            TimeUnit.SECONDS.toMillis(30);

    /* ========================================================= */
    /* ================= BASE DELAYS ============================ */
    /* ========================================================= */

    private static final long DELAY_SOFT = 1500;
    private static final long DELAY_MEDIUM = 3000;
    private static final long DELAY_HARD = 5000;

    /* ========================================================= */
    /* ================= EXTENSION DELAYS ======================= */
    /* ========================================================= */

    private static final long EXT_DELAY_BASE = 4000;
    private static final long EXT_DELAY_MED = 6000;
    private static final long EXT_DELAY_HIGH = 8000;

    /* ========================================================= */
    /* ================= ENTRY ================================= */
    /* ========================================================= */

    public Decision evaluate(
            long remainingBaseMs,
            long remainingExtensionMs
    ) {

        /* ---------- UNLIMITED ---------- */
        if (remainingBaseMs == Long.MAX_VALUE) {
            return Decision.allow();
        }

        /* ---------- BASE ---------- */
        if (remainingBaseMs > 0) {
            return evaluateBase(remainingBaseMs);
        }

        /* ---------- EXTENSION ---------- */
        if (remainingExtensionMs > 0) {
            return evaluateExtension(remainingExtensionMs);
        }

        /* ---------- BLOCK ---------- */
        return Decision.block("Daily limit reached");
    }

    /* ========================================================= */
    /* ================= BASE POLICY ============================ */
    /* ========================================================= */

    private Decision evaluateBase(long remainingBaseMs) {

        // SAFE ZONE
        if (remainingBaseMs > SOFT_WARNING_MS) {
            return Decision.allow();
        }

        // SOFT PRESSURE
        if (remainingBaseMs > HARD_WARNING_MS) {
            return Decision.delay(
                    DELAY_SOFT,
                    "Approaching daily limit",
                    false
            );
        }

        // HARD PRESSURE
        if (remainingBaseMs > CRITICAL_MS) {
            return Decision.delay(
                    DELAY_MEDIUM,
                    "Limit almost reached",
                    false
            );
        }

        // CRITICAL ZONE
        return Decision.delay(
                Math.min(DELAY_HARD, remainingBaseMs),
                "Final seconds of usage",
                false
        );
    }

    /* ========================================================= */
    /* ================= EXTENSION POLICY ======================= */
    /* ========================================================= */

    private Decision evaluateExtension(long remainingExtensionMs) {

        long delay = computeExtensionDelay(remainingExtensionMs);

        return Decision.delay(
                delay,
                buildExtensionMessage(remainingExtensionMs),
                true
        );
    }

    /* ========================================================= */
    /* ================= EXTENSION LOGIC ======================== */
    /* ========================================================= */

    private long computeExtensionDelay(long remainingExtensionMs) {

        if (remainingExtensionMs < TimeUnit.MINUTES.toMillis(1)) {
            return EXT_DELAY_HIGH;
        }

        if (remainingExtensionMs < TimeUnit.MINUTES.toMillis(5)) {
            return EXT_DELAY_MED;
        }

        return EXT_DELAY_BASE;
    }

    private String buildExtensionMessage(long remainingMs) {

        long min = TimeUnit.MILLISECONDS.toMinutes(remainingMs);

        if (min <= 1) {
            return "Last minute of extension";
        }

        if (min <= 3) {
            return "Extension ending soon";
        }

        if (min <= 5) {
            return "Extension running low";
        }

        return "Using extension time";
    }

    /* ========================================================= */
    /* ================= DECISION =============================== */
    /* ========================================================= */

    public static final class Decision {

        public final boolean blocked;
        public final long delayMs;
        public final String reason;
        public final boolean usingExtension;

        private Decision(
                boolean blocked,
                long delayMs,
                String reason,
                boolean usingExtension
        ) {
            this.blocked = blocked;
            this.delayMs = delayMs;
            this.reason = reason;
            this.usingExtension = usingExtension;
        }

        public static Decision allow() {
            return new Decision(false, 0, null, false);
        }

        public static Decision delay(
                long delayMs,
                String reason,
                boolean usingExtension
        ) {
            return new Decision(
                    false,
                    Math.max(0, delayMs),
                    reason,
                    usingExtension
            );
        }

        public static Decision block(String reason) {
            return new Decision(true, 0, reason, false);
        }
    }
}