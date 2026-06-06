package com.smthbig.shadow.launcher.apps;

import android.os.Bundle;
import android.text.InputType;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.smthbig.shadow.R;
import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.databinding.ActivityAppLimitBinding;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.repository.AppRepository;
import com.smthbig.shadow.theme.ThemeManager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AppLimitActivity extends AppCompatActivity {

    private AppLimitStore limitStore;
    private PackageManager pm;
    private AppRepository appRepository;
    private final List<AppItem> appList = new ArrayList<>();
    private AppLimitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        ActivityAppLimitBinding binding = ActivityAppLimitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeManager.applyWallpaper(this);

        ServiceLocator locator = ServiceLocator.getInstance();
        limitStore = locator.getAppLimitStore();
        appRepository = locator.getAppRepository();
        pm = getPackageManager();

        adapter = new AppLimitAdapter(this, appList);
        binding.appList.setAdapter(adapter);
        binding.appList.setOnItemClickListener((p, v, i, id) -> showLimitPicker(appList.get(i).packageName));

        loadApps();
    }

    private void loadApps() {
        appList.clear();

        List<ApplicationInfo> apps = appRepository.getLaunchableApps();

        for (ApplicationInfo app : apps) {
            String label = pm.getApplicationLabel(app).toString();
            long limit = limitStore.getLimitMs(app.packageName);
            appList.add(new AppItem(label, app.packageName, limit));
        }

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showLimitPicker(String pkg) {
        String[] options = {"No limit", "15 min", "30 min", "1 hour", "2 hours", "Custom"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Set limit for " + getAppLabel(pkg))
                .setItems(options, (d, which) -> {
                    d.dismiss();
                    switch (which) {
                        case 0:
                            limitStore.clearLimit(pkg);
                            refresh();
                            break;
                        case 1: save(pkg, 15); refresh(); break;
                        case 2: save(pkg, 30); refresh(); break;
                        case 3: save(pkg, 60); refresh(); break;
                        case 4: save(pkg, 120); refresh(); break;
                        case 5:
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .post(() -> showCustomPicker(pkg));
                            break;
                    }
                })
                .show();
    }

    private void showCustomPicker(String pkg) {
        TextInputEditText input = new TextInputEditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Minutes (min 1)");

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dpToPx(24);
        params.rightMargin = dpToPx(24);
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Custom limit (minutes)")
                .setView(container)
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        int minutes = Integer.parseInt(input.getText().toString());
                        if (minutes > 0) {
                            save(pkg, minutes);
                            refresh();
                        }
                    } catch (NumberFormatException ignored) {}
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String getAppLabel(String pkg) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception e) {
            return pkg;
        }
    }

    private void save(String pkg, int minutes) {
        limitStore.setLimitMs(pkg, TimeUnit.MINUTES.toMillis(Math.max(1, minutes)));
    }

    private void refresh() {
        loadApps();
    }
}