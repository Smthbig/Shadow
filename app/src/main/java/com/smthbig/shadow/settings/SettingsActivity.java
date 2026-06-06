package com.smthbig.shadow.settings;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.R;
import com.smthbig.shadow.databinding.ActivitySettingsBinding;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.theme.ThemeMode;
import com.smthbig.shadow.theme.ThemeRestarter;
import com.smthbig.shadow.viewmodel.SettingsViewModel;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private SettingsController controller;
    private List<SettingsItem> items;
    private SettingsViewModel viewModel;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeManager.applyWallpaper(this);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        controller = new SettingsController();

        setupToolbar(binding);
        setupItems();
        setupItemsUI(binding);
        setupObservers();
    }

    private void setupToolbar(ActivitySettingsBinding binding) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupItems() {
        items = new ArrayList<>();
        items.add(new SettingsItem("Appearance", SettingsItem.TYPE_THEME));
        items.add(new SettingsItem("App Limits", SettingsItem.TYPE_APP_LIMIT));
        items.add(new SettingsItem("Default Launcher", SettingsItem.TYPE_DEFAULT_LAUNCHER));
        items.add(new SettingsItem("Usage Access Permission", SettingsItem.TYPE_USAGE_ACCESS));
        items.add(new SettingsItem("Reset Today's Extensions", SettingsItem.TYPE_RESET_USAGE));
        items.add(new SettingsItem("Doomsday Engine", SettingsItem.TYPE_DOOMSDAY_CONFIG));
        items.add(new SettingsItem("Shadow Backgrounds", SettingsItem.TYPE_BACKGROUND_GALLERY));
        items.add(new SettingsItem("Deep Focus Mode", SettingsItem.TYPE_DEEP_FOCUS));
        items.add(new SettingsItem("Edit Whitelist", SettingsItem.TYPE_WHITELIST));
    }

    private void setupItemsUI(ActivitySettingsBinding binding) {
        if (binding.settingsContainer == null) return;
        binding.settingsContainer.removeAllViews();

        addInsightHeader(binding);

        for (SettingsItem item : items) {
            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_setting_card, binding.settingsContainer, false);

            TextView title = itemView.findViewById(R.id.title);
            if (title != null) title.setText(item.getTitle());

            itemView.setOnClickListener(v -> controller.handleItemClick(this, item));
            binding.settingsContainer.addView(itemView);
        }
    }

    private void setupObservers() {
        viewModel.getTotalInterventions().observe(this, count -> {
            // Insight header will be updated when recreated
        });
    }

    private void addInsightHeader(ActivitySettingsBinding binding) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(24));
        card.setLayoutParams(params);
        card.setRadius(dpToPx(24));
        card.setCardElevation(0);
        card.setStrokeWidth(dpToPx(1));

        int outline = getThemeColor(com.google.android.material.R.attr.colorOutline);
        int surface = getThemeColor(R.attr.shadowGlass);
        int onSurface = getThemeColor(com.google.android.material.R.attr.colorOnSurface);
        int onSurfaceVariant = getThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);

        card.setStrokeColor(outline);
        card.setCardBackgroundColor(surface);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));

        TextView title = new TextView(this);
        title.setText("Today's Focus");
        title.setTextSize(14);
        title.setAllCaps(true);
        title.setLetterSpacing(0.1f);
        title.setTextColor(onSurfaceVariant);

        Integer interventions = viewModel.getTotalInterventions().getValue();
        int count = interventions != null ? interventions : 0;

        TextView score = new TextView(this);
        score.setText(count + " Shadow Interventions");
        score.setTextSize(22);
        score.setTypeface(null, android.graphics.Typeface.BOLD);
        score.setTextColor(onSurface);
        score.setPadding(0, dpToPx(8), 0, 0);

        TextView desc = new TextView(this);
        desc.setText("Times Shadow added friction to keep you intentional.");
        desc.setTextSize(13);
        desc.setTextColor(onSurfaceVariant);
        desc.setPadding(0, dpToPx(4), 0, 0);

        layout.addView(title);
        layout.addView(score);
        layout.addView(desc);
        card.addView(layout);
        binding.settingsContainer.addView(card);
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        }
        return 0;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void openThemeDialog() {
        String[] names = {"System Default", "Light", "Dark", "Dynamic", "Shadow", "Glass",
                "Transparent Light", "Transparent Dark"};
        String[] values = {ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.DYNAMIC,
                ThemeMode.SHADOW, ThemeMode.GLASS, ThemeMode.TRANSPARENT_LIGHT, ThemeMode.TRANSPARENT_DARK};

        ThemeAdapter adapter = new ThemeAdapter(this, names, values,
                controller.getCurrentTheme(this),
                selected -> {
                    controller.applyTheme(this, selected);
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    ThemeRestarter.restart(this);
                });

        dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Select Theme")
                .setAdapter(adapter, null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }
}