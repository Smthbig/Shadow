package com.smthbig.shadow.settings;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.R;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.theme.ThemeMode;
import com.smthbig.shadow.theme.ThemeRestarter;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SettingsController controller;
    private List<SettingsItem> items;

    private AlertDialog dialog; //  keep reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeManager.apply(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        controller = new SettingsController();

        setupSystemUI();
        setupItems();
        setupList();
    }

    /* ---------- DATA ---------- */

    private void setupItems() {
        items = new ArrayList<>();
        items.add(new SettingsItem("Theme", SettingsItem.TYPE_THEME));
        items.add(new SettingsItem("App Limits", SettingsItem.TYPE_APP_LIMIT));
    }

    /* ---------- LIST ---------- */

    private void setupList() {

        ListView list = findViewById(R.id.settings_list);

        List<String> titles = new ArrayList<>();
        for (SettingsItem item : items) {
            titles.add(item.getTitle());
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);

        list.setAdapter(adapter);

        list.setOnItemClickListener(
                (p, v, i, id) -> {
                    controller.handleItemClick(this, items.get(i));
                });
    }

    /* ---------- THEME DIALOG ---------- */

    public void openThemeDialog() {

        String[] names = {"System Default", "Light", "Dark", "Dynamic", "Shadow", "Glass"};

        String[] values = {
            ThemeMode.SYSTEM,
            ThemeMode.LIGHT,
            ThemeMode.DARK,
            ThemeMode.DYNAMIC,
            ThemeMode.SHADOW,
            ThemeMode.GLASS
        };

        String current = controller.getCurrentTheme(this);

        ThemeAdapter adapter =
                new ThemeAdapter(
                        this,
                        names,
                        values,
                        current,
                        selected -> {

                            // 1. Save theme
                            controller.applyTheme(this, selected);

                            // 2. Dismiss dialog BEFORE recreate
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            // 3. Recreate activity (clean + fast)
                            ThemeRestarter.restart(this);
                        });

        dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Select Theme")
                        .setAdapter(adapter, null)
                        .setNegativeButton("Cancel", null)
                        .create();

        dialog.show();
    }

    /* ---------- SYSTEM UI ---------- */

    private void setupSystemUI() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (android.os.Build.VERSION.SDK_INT >= 30) {

            if (getWindow() != null && getWindow().getInsetsController() != null) {

                String mode = ThemeManager.get(this);

                boolean isLight = ThemeMode.LIGHT.equals(mode);

                getWindow()
                        .getInsetsController()
                        .setSystemBarsAppearance(
                                isLight
                                        ? android.view.WindowInsetsController
                                                .APPEARANCE_LIGHT_STATUS_BARS
                                        : 0,
                                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        }
    }

    /* ---------- LIFECYCLE SAFETY ---------- */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
