package com.smthbig.shadow.settings;

public class SettingsItem {

    public static final int TYPE_THEME = 1;
    public static final int TYPE_APP_LIMIT = 2;
    public static final int TYPE_DEFAULT_LAUNCHER = 3;
    public static final int TYPE_USAGE_ACCESS = 4;
    public static final int TYPE_RESET_USAGE = 5;
    public static final int TYPE_DOOMSDAY_CONFIG = 6;

    private final String title;
    private final int type;

    public SettingsItem(String title, int type) {
        this.title = title;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }
}
