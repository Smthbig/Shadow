package com.smthbig.shadow.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.R;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.theme.ThemeMode;
import com.smthbig.shadow.theme.ThemeRestarter;
import com.smthbig.shadow.tracking.UsageTracker;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SettingsController controller;
    private List<SettingsItem> items;
    private LinearLayout container;
    private UsageTracker usageTracker;

    private AlertDialog dialog; // keep reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        controller = new SettingsController();
        container = findViewById(R.id.settings_container);
        usageTracker = new UsageTracker(this);

        setupItems();
        setupItemsUI();
    }

    /* ---------- DATA ---------- */

    private void setupItems() {
        items = new ArrayList<>();
        items.add(new SettingsItem("Appearance", SettingsItem.TYPE_THEME));
        items.add(new SettingsItem("App Limits", SettingsItem.TYPE_APP_LIMIT));
        items.add(new SettingsItem("Default Launcher", SettingsItem.TYPE_DEFAULT_LAUNCHER));
        items.add(new SettingsItem("Usage Access Permission", SettingsItem.TYPE_USAGE_ACCESS));
        items.add(new SettingsItem("Reset Today's Extensions", SettingsItem.TYPE_RESET_USAGE));
        items.add(new SettingsItem("Doomsday Engine", SettingsItem.TYPE_DOOMSDAY_CONFIG));
    }

    /* ---------- LIST ---------- */

    private void setupItemsUI() {
        if (container == null) return;
        container.removeAllViews();

        addInsightHeader();

        for (SettingsItem item : items) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_setting_card, container, false);

            TextView title = itemView.findViewById(R.id.title);
            title.setText(item.getTitle());

            itemView.setOnClickListener(v -> {
                controller.handleItemClick(this, item);
            });

            container.addView(itemView);
        }
    }

    private void addInsightHeader() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 48);
        card.setLayoutParams(params);
        card.setRadius(dpToPx(24));
        card.setCardElevation(0);
        card.setStrokeWidth(1);
        card.setStrokeColor(getColor(R.color.md_outline));
        card.setCardBackgroundColor(getColor(R.color.md_surface_variant));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));

        TextView title = new TextView(this);
        title.setText("Today's Focus");
        title.setTextSize(14);
        title.setAllCaps(true);
        title.setLetterSpacing(0.1f);
        title.setTextColor(getColor(R.color.md_on_surface_variant));

        int totalFriction = usageTracker.getTotalDelays() + usageTracker.getTotalBlocks();
        TextView score = new TextView(this);
        score.setText(totalFriction + " Shadow Interventions");
        score.setTextSize(22);
        score.setTypeface(null, android.graphics.Typeface.BOLD);
        score.setTextColor(getColor(R.color.md_on_surface));
        score.setPadding(0, dpToPx(8), 0, 0);

        TextView desc = new TextView(this);
        desc.setText("Times Shadow added friction to keep you intentional.");
        desc.setTextSize(13);
        desc.setTextColor(getColor(R.color.md_on_surface_variant));
        desc.setPadding(0, dpToPx(4), 0, 0);

        layout.addView(title);
        layout.addView(score);
        layout.addView(desc);
        card.addView(layout);
        container.addView(card);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /* ---------- THEME DIALOG ---------- */

    public void openThemeDialog() {
        String[] names = {"System Default", "Light", "Dark", "Dynamic", "Shadow", "Glass", "Glass Light", "Glass Dark"};

        String[] values = {
            ThemeMode.SYSTEM,
            ThemeMode.LIGHT,
            ThemeMode.DARK,
            ThemeMode.DYNAMIC,
            ThemeMode.SHADOW,
            ThemeMode.GLASS,
            ThemeMode.GLASS_LIGHT,
            ThemeMode.GLASS_DARK
        };

        String current = controller.getCurrentTheme(this);

        ThemeAdapter adapter = new ThemeAdapter(
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

        dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Select Theme")
                .setAdapter(adapter, null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
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
