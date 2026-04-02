package com.smthbig.shadow.settings;

import android.app.Activity;
import android.content.Intent;

import com.smthbig.shadow.launcher.apps.AppLimitActivity;
import com.smthbig.shadow.theme.ThemeManager;

public class SettingsController {

    public SettingsController() {
        // stateless → safe
    }

    /* ---------- ACTION HANDLER ---------- */

    public void handleItemClick(SettingsActivity activity, SettingsItem item) {

        switch (item.getType()) {

            case SettingsItem.TYPE_THEME:
                activity.openThemeDialog(); // UI handled by Activity
                break;

            case SettingsItem.TYPE_APP_LIMIT:
                openAppLimits(activity);
                break;
        }
    }

    /* ---------- NAVIGATION ---------- */

    private void openAppLimits(Activity activity) {
        activity.startActivity(
                new Intent(activity, AppLimitActivity.class)
        );
    }

    /* ---------- THEME ---------- */

    public String getCurrentTheme(Activity activity) {
        return ThemeManager.get(activity); //  updated API
    }

    public void applyTheme(Activity activity, String mode) {
        ThemeManager.set(activity, mode); // updated API
    }
}