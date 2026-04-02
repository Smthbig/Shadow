package com.smthbig.shadow.settings;

public class SettingsItem {

    public static final int TYPE_THEME = 0;
    public static final int TYPE_APP_LIMIT = 1;

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
