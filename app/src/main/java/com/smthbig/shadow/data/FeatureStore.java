package com.smthbig.shadow.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public final class FeatureStore {

    private static final String PREF = "shadow_features";
    private final SharedPreferences prefs;

    // Background Types
    public static final String BG_DEFAULT = "default";
    public static final String BG_DISTORTED = "distorted";
    public static final String BG_MESH = "mesh";
    public static final String BG_AURORA = "aurora";
    public static final String BG_SYSTEM_WALLPAPER = "system_wallpaper";

    public FeatureStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public String getBackgroundType() {
        return prefs.getString("background_type", BG_DEFAULT);
    }

    public void setBackgroundType(String type) {
        prefs.edit().putString("background_type", type).apply();
    }

    public boolean isWallpaperEnabled() {
        return BG_SYSTEM_WALLPAPER.equals(getBackgroundType());
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
