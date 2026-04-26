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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.smthbig.shadow.R;
import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.launcher.core.LauncherController;
import com.smthbig.shadow.settings.SettingsActivity;
import com.smthbig.shadow.theme.ThemeManager;

import java.util.List;

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
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        featureStore = new FeatureStore(this);
        launcherController = new LauncherController(this);

        prepareWindow();
        setContentView(R.layout.activity_home);
        ThemeManager.applyWallpaper(this);

        contentContainer = findViewById(R.id.content_container);
        blurOverlay = findViewById(R.id.blur_overlay);
        backgroundLayer = findViewById(R.id.background_layer);
        doomsdayView = findViewById(R.id.doomsday);
        settingsBtn = findViewById(R.id.settings_btn);
        intentBar = findViewById(R.id.intent_bar);

        if (doomsdayView != null) doomsdayView.updateState();
        if (settingsBtn != null) settingsBtn.setOnClickListener(v -> openSettings());
        if (contentContainer != null) contentContainer.setOnClickListener(v -> showSearch());
        if (blurOverlay != null) blurOverlay.setOnClickListener(v -> hideSearch());

        setupIntentBar();
        setupBackHandler();
        setupKeyboardAnimation();
    }

    /**
     * 🚀 PIXEL-PERFECT KEYBOARD SYNC
     * This follows the keyboard's exact movement frame-by-frame.
     */
    private void setupKeyboardAnimation() {
        if (intentBar == null) return;

        ViewCompat.setWindowInsetsAnimationCallback(intentBar, new WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
        ) {
            @Override
            public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                super.onPrepare(animation);
            }

            @NonNull
            @Override
            public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                int navHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                
                int offset = Math.max(0, imeHeight - navHeight);
                intentBar.setTranslationY(-offset);
                
                return insets;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeManager.applyWallpaper(this);
        if (doomsdayView != null) doomsdayView.updateState();
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
        blurOverlay.setVisibility(View.VISIBLE);
        blurOverlay.setAlpha(0f);

        blurOverlay.animate().alpha(1f).setDuration(250).start();
        intentBar.animate()
                .alpha(1f)
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
                .setDuration(250)
                .withEndAction(() -> {
                    intentBar.setVisibility(View.GONE);
                    blurOverlay.setVisibility(View.GONE);
                    clearBlur();
                    isAnimating = false;
                })
                .start();
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
}
