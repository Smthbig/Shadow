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

        limitStore = new AppLimitStore(this);
        pm = getPackageManager();

        ListView listView = findViewById(R.id.app_list);

        adapter = new AppLimitAdapter(this, appList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                (p, v, i, id) -> showLimitPicker(appList.get(i).packageName));

        loadApps(); // load after adapter bind
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

        com.google.android.material.textfield.TextInputEditText input = 
                new com.google.android.material.textfield.TextInputEditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Minutes");

        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dpToPx(24);
        params.rightMargin = dpToPx(24);
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Custom limit")
                .setView(container)
                .setPositiveButton(
                        "Save",
                        (d, w) -> {
                            try {
                                String val = input.getText().toString();
                                if (!val.isEmpty()) {
                                    save(pkg, Integer.parseInt(val));
                                    refresh();
                                }
                            } catch (Exception ignored) {}
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /* ---------- HELPERS ---------- */

    private void save(String pkg, int minutes) {
        limitStore.setLimitMs(pkg, TimeUnit.MINUTES.toMillis(minutes));
    }

    private void refresh() {
        loadApps();
    }
}
