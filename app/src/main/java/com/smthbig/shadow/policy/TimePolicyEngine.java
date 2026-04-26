package com.smthbig.shadow.policy;

import java.util.concurrent.TimeUnit;

public final class TimePolicyEngine {

    // Threshold: Start adding friction when 70% of limit is REMAINING (30% used)
    private static final float FRICTION_START_THRESHOLD = 0.7f;
    
    // Maximum base delay (ms) when limit is nearly 0
    private static final long MAX_BASE_DELAY = 10000; // 10 seconds

    public Decision evaluate(
            long remainingBaseMs,
            long totalLimitMs,
            long remainingExtensionMs,
            long heatPenaltyMs
    ) {
        // 1. UNLIMITED
        if (totalLimitMs <= 0 || remainingBaseMs == Long.MAX_VALUE) {
            return Decision.allow();
        }

        // 2. BASE LIMIT FLOW
        if (remainingBaseMs > 0) {
            float percentRemaining = (float) remainingBaseMs / totalLimitMs;
            
            if (percentRemaining > FRICTION_START_THRESHOLD) {
                // No friction yet, but still apply heat penalty if user is spamming
                if (heatPenaltyMs > 0) {
                    return Decision.delay(heatPenaltyMs, "Slow down...", false);
                }
                return Decision.allow();
            }

            // Calculate incremental delay
            // As percentRemaining goes from 0.7 -> 0.0, progress goes 0.0 -> 1.0
            float progress = (FRICTION_START_THRESHOLD - percentRemaining) / FRICTION_START_THRESHOLD;
            long dynamicDelay = (long) (progress * MAX_BASE_DELAY);
            
            return Decision.delay(
                    dynamicDelay + heatPenaltyMs,
                    "Limit decreasing, delay increasing",
                    false
            );
        }

        // 3. EXTENSION FLOW
        if (remainingExtensionMs > 0) {
            // Extension always has a high base friction to discourage use
            long extDelay = 5000 + heatPenaltyMs; 
            return Decision.delay(extDelay, "Using limited extension time", true);
        }

        // 4. OVER LIMIT (NO EXTENSION YET)
        // Instead of blocking, show a high-friction delay screen that allows extension
        return Decision.delay(10000, "Limit reached. Add extension to continue.", false);
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