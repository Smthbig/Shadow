package com.smthbig.shadow.launcher.home;

import android.content.Context;
import android.content.SharedPreferences;

public final class DoomsdayStore {

    private static final String PREF = "doomsday_settings";
    
    public enum Scale { WEEK, MONTH, YEAR, CUSTOM }

    private final SharedPreferences prefs;

    public DoomsdayStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public Scale getScale() {
        String s = prefs.getString("scale", Scale.WEEK.name());
        try {
            return Scale.valueOf(s);
        } catch (Exception e) {
            return Scale.WEEK;
        }
    }

    public void setScale(Scale scale) {
        prefs.edit().putString("scale", scale.name()).apply();
    }

    public int getCustomDays() {
        return prefs.getInt("custom_days", 100);
    }

    public void setCustomDays(int days) {
        prefs.edit().putInt("custom_days", Math.max(1, days)).apply();
    }

    public int getActiveColor() {
        return prefs.getInt("color_active", 0xCCFFFFFF);
    }

    public void setActiveColor(int color) {
        prefs.edit().putInt("color_active", color).apply();
    }

    public int getInactiveColor() {
        return prefs.getInt("color_inactive", 0x33FFFFFF);
    }

    public void setInactiveColor(int color) {
        prefs.edit().putInt("color_inactive", color).apply();
    }
}
