package com.smthbig.shadow.launcher.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public final class DoomsdayStore {

    private static final String PREF = "doomsday_settings";
    
    public enum Scale { WEEK, MONTH, YEAR }

    private final SharedPreferences prefs;

    public DoomsdayStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public Scale getScale() {
        String s = prefs.getString("scale", Scale.WEEK.name());
        return Scale.valueOf(s);
    }

    public void setScale(Scale scale) {
        prefs.edit().putString("scale", scale.name()).apply();
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
