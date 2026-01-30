package com.smthbig.shadow.behavior;

public final class TimePolicyEngine {

    public enum DecisionType {
        ALLOW,
        DELAY,
        BLOCK
    }

    public static final class Decision {
        public final DecisionType type;
        public final long delayMs;
        public final String reason;

        private Decision(DecisionType type, long delayMs, String reason) {
            this.type = type;
            this.delayMs = delayMs;
            this.reason = reason;
        }

        public static Decision allow() {
            return new Decision(DecisionType.ALLOW, 0, null);
        }

        public static Decision delay(long delayMs, String reason) {
            return new Decision(DecisionType.DELAY, delayMs, reason);
        }

        public static Decision block(String reason) {
            return new Decision(DecisionType.BLOCK, 0, reason);
        }
    }

    public Decision evaluate(
            long baseLimitMs,
            long baseUsedMs,
            long extensionGrantedMs,
            long extensionUsedMs,
            int extensionCount
    ) {
        // Base time still available → allow
        if (baseUsedMs < baseLimitMs) {
            return Decision.allow();
        }

        long extensionRemaining =
                extensionGrantedMs - extensionUsedMs;

        // Extension available → small delay to make user aware
        if (extensionRemaining > 0) {
            return Decision.delay(
                    3_000,
                    "Daily limit reached.\nUsing extension time."
            );
        }

        // No time left → hard block
        return Decision.block(
                "Daily limit finished.\nCome back tomorrow."
        );
    }
}