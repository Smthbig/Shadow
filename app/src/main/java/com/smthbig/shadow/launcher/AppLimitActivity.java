package com.smthbig.shadow.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;

import com.smthbig.shadow.R;
import com.smthbig.shadow.data.limits.AppLimitStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AppLimitActivity extends Activity {

    private AppLimitStore limitStore;
    private PackageManager pm;

    private final List<AppItem> appList = new ArrayList<>();
    private AppLimitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_limit);

        limitStore = new AppLimitStore(this);
        pm = getPackageManager();

        ListView listView = findViewById(R.id.app_list);

        loadApps();

        adapter = new AppLimitAdapter(this, appList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> p, android.view.View v, int i, long id) ->
                showLimitPicker(appList.get(i).packageName)
        );
    }

    /* ---------- LOAD APPS ---------- */

    private void loadApps() {
        appList.clear();

        List<ApplicationInfo> apps =
                pm.getInstalledApplications(0);

        List<ApplicationInfo> launchable = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                launchable.add(app);
            }
        }

        Collections.sort(
                launchable,
                Comparator.comparing(
                        a -> pm.getApplicationLabel(a).toString().toLowerCase()
                )
        );

        for (ApplicationInfo app : launchable) {
            String label =
                    pm.getApplicationLabel(app).toString();

            long limit = limitStore.getLimitMs(app.packageName);

            appList.add(new AppItem(label, app.packageName, limit));
        }
    }

    /* ---------- PICKER ---------- */

    private void showLimitPicker(String pkg) {

        String[] options = {
                "Unlimited",
                "15 min",
                "30 min",
                "60 min",
                "120 min",
                "Custom"
        };

        new AlertDialog.Builder(this)
                .setTitle("Set limit")
                .setItems(options, (d, which) -> {

                    switch (which) {
                        case 0:
                            limitStore.setLimitMs(pkg, -1);
                            break;
                        case 1:
                            save(pkg, 15);
                            break;
                        case 2:
                            save(pkg, 30);
                            break;
                        case 3:
                            save(pkg, 60);
                            break;
                        case 4:
                            save(pkg, 120);
                            break;
                        case 5:
                            showCustomPicker(pkg);
                            return;
                    }

                    refresh();
                })
                .show();
    }

    private void showCustomPicker(String pkg) {
        android.widget.NumberPicker picker = new android.widget.NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(300);
        picker.setValue(60);

        new AlertDialog.Builder(this)
                .setTitle("Custom limit")
                .setView(picker)
                .setPositiveButton("Save", (d, w) -> {
                    save(pkg, picker.getValue());
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void save(String pkg, int minutes) {
        limitStore.setLimitMs(pkg,
                TimeUnit.MINUTES.toMillis(minutes));
    }

    private void refresh() {
        loadApps();
        adapter.notifyDataSetChanged();
    }
}