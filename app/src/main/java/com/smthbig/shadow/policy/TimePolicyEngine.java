package com.smthbig.shadow.policy;

import android.util.Log;

import java.util.concurrent.TimeUnit;

public final class TimePolicyEngine {

    private static final String TAG = "TimePolicy";

    private static final float FRICTION_START_THRESHOLD = 0.7f;
    private static final long MAX_BASE_DELAY_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long EXTENSION_BASE_DELAY_MS = TimeUnit.SECONDS.toMillis(5);
    private static final long OVER_LIMIT_DELAY_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long MAX_TOTAL_DELAY_MS = TimeUnit.SECONDS.toMillis(30);

    public Decision evaluate(
            long remainingBaseMs,
            long totalLimitMs,
            long remainingExtensionMs,
            long heatPenaltyMs
    ) {
        if (totalLimitMs <= 0 || remainingBaseMs == Long.MAX_VALUE) {
            return Decision.allow();
        }

        if (remainingBaseMs > 0) {
            float percentRemaining = (float) remainingBaseMs / totalLimitMs;

            if (percentRemaining > FRICTION_START_THRESHOLD) {
                if (heatPenaltyMs > 0) {
                    long capped = Math.min(heatPenaltyMs, MAX_TOTAL_DELAY_MS);
                    return Decision.delay(capped, "Slow down...", false);
                }
                return Decision.allow();
            }

            float progress = (FRICTION_START_THRESHOLD - percentRemaining) / FRICTION_START_THRESHOLD;
            long dynamicDelay = (long) (progress * MAX_BASE_DELAY_MS);
            long totalDelay = Math.min(dynamicDelay + heatPenaltyMs, MAX_TOTAL_DELAY_MS);

            Log.d(TAG, "Friction: remaining=" + percentRemaining
                    + " dynamic=" + dynamicDelay + " heat=" + heatPenaltyMs
                    + " total=" + totalDelay);

            return Decision.delay(totalDelay, "Limit decreasing, delay increasing", false);
        }

        if (remainingExtensionMs > 0) {
            long totalDelay = Math.min(EXTENSION_BASE_DELAY_MS + heatPenaltyMs, MAX_TOTAL_DELAY_MS);
            return Decision.delay(totalDelay, "Using limited extension time", true);
        }

        return Decision.delay(OVER_LIMIT_DELAY_MS,
                "Limit reached. Add extension to continue.", false);
    }

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
            return new Decision(false, Math.max(0, delayMs), reason, usingExtension);
        }

        public static Decision block(String reason) {
            return new Decision(true, 0, reason, false);
        }
    }
}
