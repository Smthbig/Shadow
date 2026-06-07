package com.smthbig.shadow.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.launcher.apps.AppLimitActivity;
import com.smthbig.shadow.launcher.home.DoomsdayStore;
import com.smthbig.shadow.repository.AppRepository;
import com.smthbig.shadow.setup.permissions.UsagePermissionHelper;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.theme.ThemeRestarter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SettingsController {

    public SettingsController() {}

    public void handleItemClick(SettingsActivity activity, SettingsItem item) {
        switch (item.getType()) {
            case SettingsItem.TYPE_THEME:
                activity.openThemeDialog();
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
            case SettingsItem.TYPE_DOOMSDAY_CONFIG:
                openDoomsdayConfig(activity);
                break;
            case SettingsItem.TYPE_BACKGROUND_GALLERY:
                openBackgroundGallery(activity);
                break;
            case SettingsItem.TYPE_DEEP_FOCUS:
                toggleDeepFocus(activity);
                break;
            case SettingsItem.TYPE_WHITELIST:
                openWhitelistPicker(activity);
                break;
        }
    }

    private void openDoomsdayConfig(Activity activity) {
        String[] options = {"Change Scale", "Custom Day Count", "Active Dot Color", "Inactive Dot Color"};
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Doomsday View")
                .setItems(options, (d, which) -> {
                    if (which == 0) openDoomsdayScalePicker(activity);
                    if (which == 1) openCustomDaysPicker(activity);
                    if (which == 2) openDoomsdayColorPicker(activity, true);
                    if (which == 3) openDoomsdayColorPicker(activity, false);
                })
                .show();
    }

    private void openDoomsdayScalePicker(Activity activity) {
        String[] scales = {"Week (7 days)", "Month (current)", "Year (365 days)", "Custom Count"};
        DoomsdayStore store = new DoomsdayStore(activity);
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Select Scale")
                .setItems(scales, (d, which) -> {
                    if (which == 0) store.setScale(DoomsdayStore.Scale.WEEK);
                    if (which == 1) store.setScale(DoomsdayStore.Scale.MONTH);
                    if (which == 2) store.setScale(DoomsdayStore.Scale.YEAR);
                    if (which == 3) store.setScale(DoomsdayStore.Scale.CUSTOM);
                })
                .show();
    }

    private void openCustomDaysPicker(Activity activity) {
        DoomsdayStore store = new DoomsdayStore(activity);
        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(store.getCustomDays()));

        FrameLayout container = new FrameLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Enter Day Count")
                .setView(container)
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        store.setCustomDays(Integer.parseInt(input.getText().toString()));
                        store.setScale(DoomsdayStore.Scale.CUSTOM);
                    } catch (NumberFormatException ignored) {}
                })
                .show();
    }

    private void openDoomsdayColorPicker(Activity activity, boolean active) {
        String[] names = {"Theme Default", "Classic White", "Focus Purple",
                "Flux Dark", "Emerald", "Amber", "Crimson", "Ocean Blue"};
        int[] colors = {0, 0xCCFFFFFF, 0xCC6750A4, 0xCC121212, 0xCC2E7D32, 0xCCFF8F00, 0xCCB71C1C, 0xCC1565C0};
        DoomsdayStore store = new DoomsdayStore(activity);
        new MaterialAlertDialogBuilder(activity)
                .setTitle(active ? "Active Color" : "Inactive Color")
                .setItems(names, (d, which) -> {
                    if (which == 0) {
                        if (active) store.resetActiveColor();
                        else store.resetInactiveColor();
                    } else {
                        if (active) store.setActiveColor(colors[which]);
                        else store.setInactiveColor(colors[which]);
                    }
                })
                .show();
    }

    private void openAppLimits(Activity activity) {
        activity.startActivity(new Intent(activity, AppLimitActivity.class));
    }

    private void openDefaultLauncherSettings(Activity activity) {
        Intent intent = UsagePermissionHelper.getHomeRoleRequestIntent(activity);
        if (intent != null) activity.startActivity(intent);
    }

    private void openUsageAccessSettings(Activity activity) {
        activity.startActivity(UsagePermissionHelper.getUsageAccessIntent());
    }

    private void resetUsage(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Reset Extensions?")
                .setMessage("Clear all extra time granted today?")
                .setPositiveButton("Reset", (d, w) -> {
                    new ExtensionEngine(activity).clearAll();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openBackgroundGallery(Activity activity) {
        String[] options = {"Gradient", "Distorted", "Mesh",
                "Aurora", "System Wallpaper"};
        String[] types = {FeatureStore.BG_DEFAULT, FeatureStore.BG_DISTORTED,
                FeatureStore.BG_MESH, FeatureStore.BG_AURORA, FeatureStore.BG_SYSTEM_WALLPAPER};

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Backgrounds")
                .setItems(options, (d, which) -> {
                    ThemeManager.setBackground(activity, types[which]);
                    ThemeRestarter.restart(activity);
                })
                .show();
    }

    private void toggleDeepFocus(Activity activity) {
        FeatureStore store = new FeatureStore(activity);
        boolean enabled = !store.isDeepFocusEnabled();
        store.setDeepFocusEnabled(enabled);
    }

    private void openWhitelistPicker(Activity activity) {
        android.app.AlertDialog loading = new android.app.AlertDialog.Builder(activity)
                .setTitle("Loading...")
                .setMessage("Loading installed apps...")
                .setCancelable(false)
                .show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppRepository repo = ServiceLocator.getInstance().getAppRepository();
                PackageManager pm = activity.getPackageManager();
                List<ApplicationInfo> launchable = repo.getLaunchableApps();
                FeatureStore store = new FeatureStore(activity);

                String[] names = new String[launchable.size()];
                boolean[] checked = new boolean[launchable.size()];

                for (int i = 0; i < launchable.size(); i++) {
                    names[i] = pm.getApplicationLabel(launchable.get(i)).toString();
                    checked[i] = store.isWhitelisted(launchable.get(i).packageName);
                }

                activity.runOnUiThread(() -> {
                    loading.dismiss();
                    new MaterialAlertDialogBuilder(activity)
                            .setTitle("Whitelist")
                            .setMultiChoiceItems(names, checked,
                                    (d, which, isChecked) ->
                                            store.toggleWhitelist(launchable.get(which).packageName))
                            .setPositiveButton("Done", null)
                            .show();
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    loading.dismiss();
                });
            }
        });
    }

    public String getCurrentTheme(Activity activity) {
        return ThemeManager.getTheme(activity);
    }

    public void applyTheme(Activity activity, String mode) {
        ThemeManager.setTheme(activity, mode);
    }
}