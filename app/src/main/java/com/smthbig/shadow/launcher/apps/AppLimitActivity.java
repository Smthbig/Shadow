package com.smthbig.shadow.launcher.apps;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.R;
import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.theme.ThemeMode;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AppLimitActivity extends AppCompatActivity {

    private AppLimitStore limitStore;
    private PackageManager pm;

    private final List<AppItem> appList = new ArrayList<>();
    private AppLimitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_limit);

        setupSystemUI();

        limitStore = new AppLimitStore(this);
        pm = getPackageManager();

        ListView listView = findViewById(R.id.app_list);

        adapter = new AppLimitAdapter(this, appList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                (p, v, i, id) -> showLimitPicker(appList.get(i).packageName));

        loadApps(); // load after adapter bind
    }

    /* ---------- SYSTEM UI ---------- */

    private void setupSystemUI() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (android.os.Build.VERSION.SDK_INT >= 30) {

            if (getWindow().getInsetsController() != null) {

                boolean isLight = ThemeMode.LIGHT.equals(ThemeManager.get(this));

                getWindow()
                        .getInsetsController()
                        .setSystemBarsAppearance(
                                isLight ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        }
    }

    /* ---------- LOAD APPS ---------- */

    private void loadApps() {

        appList.clear();

        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<ApplicationInfo> launchable = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                launchable.add(app);
            }
        }

        Collections.sort(
                launchable,
                Comparator.comparing(a -> pm.getApplicationLabel(a).toString().toLowerCase()));

        for (ApplicationInfo app : launchable) {

            String label = pm.getApplicationLabel(app).toString();
            long limit = limitStore.getLimitMs(app.packageName);

            appList.add(new AppItem(label, app.packageName, limit));
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /* ---------- LIMIT PICKER ---------- */

    private void showLimitPicker(String pkg) {

        String[] options = {"Unlimited", "15 min", "30 min", "1 hour", "2 hours", "Custom"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Set limit")
                .setItems(
                        options,
                        (d, which) -> {
                            d.dismiss(); // 🔥 IMPORTANT FIX

                            switch (which) {
                                case 0:
                                    limitStore.setLimitMs(pkg, -1);
                                    refresh();
                                    break;

                                case 1:
                                    save(pkg, 15);
                                    refresh();
                                    break;

                                case 2:
                                    save(pkg, 30);
                                    refresh();
                                    break;

                                case 3:
                                    save(pkg, 60);
                                    refresh();
                                    break;

                                case 4:
                                    save(pkg, 120);
                                    refresh();
                                    break;

                                case 5:
                                    // 🔥 delay to avoid window conflict
                                    new android.os.Handler(android.os.Looper.getMainLooper())
                                            .post(() -> showCustomPicker(pkg));
                                    break;
                            }
                        })
                .show();
    }

    private void showCustomPicker(String pkg) {

        android.widget.NumberPicker picker = new android.widget.NumberPicker(this);

        picker.setMinValue(1);
        picker.setMaxValue(300);
        picker.setValue(60);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Custom limit (minutes)")
                .setView(picker)
                .setPositiveButton(
                        "Save",
                        (d, w) -> {
                            save(pkg, picker.getValue());
                            refresh();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ---------- HELPERS ---------- */

    private void save(String pkg, int minutes) {
        limitStore.setLimitMs(pkg, TimeUnit.MINUTES.toMillis(minutes));
    }

    private void refresh() {
        loadApps();
    }
}
