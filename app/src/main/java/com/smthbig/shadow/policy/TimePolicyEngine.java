package com.smthbig.shadow.policy;

import java.util.concurrent.TimeUnit;

public final class TimePolicyEngine {

    // Small pause before launch when base time is almost over
    private static final long BASE_WARNING_THRESHOLD_MS =
            TimeUnit.MINUTES.toMillis(2);

    // Short intentional friction when extension time is used
    private static final long EXTENSION_LAUNCH_DELAY_MS =
            TimeUnit.SECONDS.toMillis(5);

    public Decision evaluate(
            long remainingBaseMs,
            long remainingExtensionMs
    ) {
        // Normal usage — plenty of base time left
        if (remainingBaseMs > BASE_WARNING_THRESHOLD_MS) {
            return Decision.allow();
        }

        // Base time almost finished — allow but warn
        if (remainingBaseMs > 0) {
            return Decision.delay(
                    Math.min(
                            EXTENSION_LAUNCH_DELAY_MS,
                            remainingBaseMs
                    ),
                    "Daily time almost finished",
                    false
            );
        }

        // Base exhausted, extension available
        if (remainingExtensionMs > 0) {
            return Decision.delay(
                    Math.min(
                            EXTENSION_LAUNCH_DELAY_MS,
                            remainingExtensionMs
                    ),
                    "Using extension time",
                    true
            );
        }

        // Fully exhausted
        return Decision.block(
                "Daily limit reached"
        );
    }

    /* ---------- Decision ---------- */

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
            return new Decision(
                    false,
                    0,
                    null,
                    false
            );
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
            return new Decision(
                    true,
                    0,
                    reason,
                    false
            );
        }
    }
}