package com.smthbig.shadow.tracking;

public final class UsageSnapshot {

    public final String packageName;
    public final long usedMs;

    public UsageSnapshot(String packageName, long usedMs) {
        this.packageName = packageName;
        this.usedMs = usedMs;
    }

    public long getUsedMinutes() {
        return usedMs / (60 * 1000);
    }
}