package com.smthbig.shadow.launcher;

public class AppItem {
    public final String label;
    public final String packageName;
    public final long limitMs;

    public AppItem(String label, String pkg, long limit) {
        this.label = label;
        this.packageName = pkg;
        this.limitMs = limit;
    }
}