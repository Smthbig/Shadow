package com.smthbig.shadow.settings;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.launcher.apps.AppLimitActivity;
import com.smthbig.shadow.setup.permissions.UsagePermissionHelper;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.extension.ExtensionEngine;

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

            case SettingsItem.TYPE_DEFAULT_LAUNCHER:
                openDefaultLauncherSettings(activity);
                break;

            case SettingsItem.TYPE_USAGE_ACCESS:
                openUsageAccessSettings(activity);
                break;

            case SettingsItem.TYPE_RESET_USAGE:
                resetUsage(activity);
                break;
        }
    }

    /* ---------- NAVIGATION ---------- */

    private void openAppLimits(Activity activity) {
        activity.startActivity(
                new Intent(activity, AppLimitActivity.class)
        );
    }

    private void openDefaultLauncherSettings(Activity activity) {
        Intent intent = UsagePermissionHelper.getHomeRoleRequestIntent(activity);
        if (intent != null) {
            activity.startActivity(intent);
        }
    }

    private void openUsageAccessSettings(Activity activity) {
        activity.startActivity(UsagePermissionHelper.getUsageAccessIntent());
    }

    private void resetUsage(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Reset Today's Extensions?")
                .setMessage("This will clear all extension time granted for today. Base limits (from system) cannot be reset.")
                .setPositiveButton("Reset", (d, w) -> {
                    ExtensionEngine engine = new ExtensionEngine(activity);
                    engine.clearAll();
                    android.widget.Toast.makeText(activity, "Extension time reset", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ---------- THEME ---------- */

    public String getCurrentTheme(Activity activity) {
        return ThemeManager.get(activity); //  updated API
    }

    public void applyTheme(Activity activity, String mode) {
        ThemeManager.set(activity, mode); // updated API
    }
}
