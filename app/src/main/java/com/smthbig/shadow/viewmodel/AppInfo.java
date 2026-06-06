package com.smthbig.shadow.viewmodel;

public final class AppInfo {
    public final String label;
    public final String packageName;
    public final boolean whitelisted;

    public AppInfo(String label, String packageName, boolean whitelisted) {
        this.label = label;
        this.packageName = packageName;
        this.whitelisted = whitelisted;
    }
}
