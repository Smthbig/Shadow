package com.smthbig.shadow.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public final class FeatureStore {

    private static final String PREF = "shadow_features";
    private final SharedPreferences prefs;

    public FeatureStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isWallpaperEnabled() {
        return prefs.getBoolean("wallpaper_enabled", false);
    }

    public void setWallpaperEnabled(boolean enabled) {
        prefs.edit().putBoolean("wallpaper_enabled", enabled).apply();
    }

    public boolean isDeepFocusEnabled() {
        return prefs.getBoolean("deep_focus", false);
    }

    public void setDeepFocusEnabled(boolean enabled) {
        prefs.edit().putBoolean("deep_focus", enabled).apply();
    }

    public Set<String> getWhitelist() {
        return prefs.getStringSet("whitelist", new HashSet<>());
    }

    public void toggleWhitelist(String pkg) {
        Set<String> list = new HashSet<>(getWhitelist());
        if (list.contains(pkg)) {
            list.remove(pkg);
        } else {
            list.add(pkg);
        }
        prefs.edit().putStringSet("whitelist", list).apply();
    }

    public boolean isWhitelisted(String pkg) {
        return getWhitelist().contains(pkg);
    }
}
