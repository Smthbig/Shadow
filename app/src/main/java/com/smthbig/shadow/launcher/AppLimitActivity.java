package com.smthbig.shadow.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.smthbig.shadow.data.limits.AppLimitStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AppLimitActivity extends Activity {

    private AppLimitStore limitStore;
    private PackageManager pm;

    private final List<String> labels = new ArrayList<>();
    private final List<String> packages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limitStore = new AppLimitStore(this);
        pm = getPackageManager();

        setContentView(createList());
    }

    private View createList() {
        ListView listView = new ListView(this);
        listView.setBackgroundColor(Color.BLACK);

        loadApps();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        labels
                );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                (p, v, i, id) ->
                        showLimitPicker(
                                labels.get(i),
                                packages.get(i)
                        )
        );

        return listView;
    }

    private void loadApps() {
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

            long limitMin =
                    TimeUnit.MILLISECONDS.toMinutes(
                            limitStore.getLimitMs(app.packageName)
                    );

            labels.add(label + "  (" + limitMin + " min)");
            packages.add(app.packageName);
        }
    }

    private void showLimitPicker(String label, String pkg) {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(5);     // safety floor
        picker.setMaxValue(240);   // 4 hours
        picker.setWrapSelectorWheel(false);

        picker.setValue(
                (int) TimeUnit.MILLISECONDS.toMinutes(
                        limitStore.getLimitMs(pkg)
                )
        );

        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(Color.WHITE);
        title.setTextSize(18f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 32, 0, 32);

        new AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setView(picker)
                .setPositiveButton(
                        "Save",
                        (d, w) -> {
                            limitStore.setLimitMs(
                                    pkg,
                                    TimeUnit.MINUTES.toMillis(
                                            picker.getValue()
                                    )
                            );
                            recreate(); // refresh list labels
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}