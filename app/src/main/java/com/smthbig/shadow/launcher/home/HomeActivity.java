package com.smthbig.shadow.launcher.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.smthbig.shadow.R;
import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.launcher.core.LauncherController;
import com.smthbig.shadow.settings.SettingsActivity;
import com.smthbig.shadow.theme.ThemeManager;

public class HomeActivity extends AppCompatActivity {

    private LauncherController launcherController;
    private FeatureStore featureStore;

    private IntentBarView intentBar;
    private FrameLayout contentContainer;
    private View blurOverlay;
    private View backgroundLayer;
    private DoomsdayView doomsdayView;
    private ImageView settingsBtn;

    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        featureStore = new FeatureStore(this);
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        prepareWindow();
        launcherController = new LauncherController(this);

        setContentView(R.layout.activity_home);

        contentContainer = findViewById(R.id.content_container);
        blurOverlay = findViewById(R.id.blur_overlay);
        backgroundLayer = findViewById(R.id.background_layer);
        doomsdayView = findViewById(R.id.doomsday);
        settingsBtn = findViewById(R.id.settings_btn);
        intentBar = findViewById(R.id.intent_bar);

        applyBackgroundConfig();

        if (doomsdayView != null) {
            doomsdayView.updateState();
        }

        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> openSettings());
        }

        if (contentContainer != null) {
            contentContainer.setOnClickListener(v -> showSearch());
        }

        if (blurOverlay != null) {
            blurOverlay.setOnClickListener(v -> hideSearch());
        }

        setupIntentBar();
        setupBackHandler();
    }

    private void setupIntentBar() {
        if (intentBar == null) return;
        intentBar.setCallback(new IntentBarView.Callback() {
            @Override
            public void onIntentEntered(String text) {
                hideSearch();
                launcherController.handleIntentText(text);
            }

            @Override
            public void onDismiss() {
                hideSearch();
            }
        });
    }

    private void showSearch() {
        if (isAnimating || intentBar == null || intentBar.getVisibility() == View.VISIBLE) return;
        isAnimating = true;

        applyBlur(12f);

        intentBar.setVisibility(View.VISIBLE);
        intentBar.setAlpha(0f);
        intentBar.setTranslationY(dpToPx(40));

        blurOverlay.setVisibility(View.VISIBLE);
        blurOverlay.setAlpha(0f);

        blurOverlay.animate().alpha(1f).setDuration(250).start();
        intentBar.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(300)
                .withEndAction(() -> {
                    isAnimating = false;
                    intentBar.focus();
                })
                .start();
    }

    private void hideSearch() {
        if (isAnimating || intentBar == null || intentBar.getVisibility() != View.VISIBLE) return;
        isAnimating = true;

        intentBar.clearFocusAndHide();

        blurOverlay.animate().alpha(0f).setDuration(200).start();
        intentBar.animate()
                .alpha(0f)
                .translationY(dpToPx(40))
                .setDuration(250)
                .withEndAction(() -> {
                    intentBar.setVisibility(View.GONE);
                    blurOverlay.setVisibility(View.GONE);
                    clearBlur();
                    isAnimating = false;
                })
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (doomsdayView != null) {
            doomsdayView.updateState();
        }
    }

    private void applyBackgroundConfig() {
        if (backgroundLayer == null) return;
        ThemeManager.apply(this); // Refreshes both theme and wallpaper
    }

    private void prepareWindow() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.hide(android.view.WindowInsets.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (intentBar != null && intentBar.getVisibility() == View.VISIBLE) {
                    hideSearch();
                }
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void applyBlur(float radius) {
        if (Build.VERSION.SDK_INT >= 31 && contentContainer != null) {
            contentContainer.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
        }
    }

    private void clearBlur() {
        if (Build.VERSION.SDK_INT >= 31 && contentContainer != null) {
            contentContainer.setRenderEffect(null);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private int getThemedColor(int attr) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        }
        return 0x33000000;
    }
}
