package com.smthbig.shadow.launcher.home;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.widget.FrameLayout;

import com.smthbig.shadow.R;
import com.smthbig.shadow.settings.SettingsActivity;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.launcher.core.LauncherController;

public class HomeActivity extends AppCompatActivity {

    private LauncherController launcherController;

    private IntentBarView intentBar;

    //  NEW LAYERS
    private FrameLayout contentContainer;
    private FrameLayout overlayContainer;
    private View blurOverlay;

    private float downY;
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        prepareWindow();

        launcherController = new LauncherController(this);

        setContentView(R.layout.activity_home);

        // bind correct layers
        contentContainer = findViewById(R.id.content_container);
        overlayContainer = findViewById(R.id.overlay_container);
        blurOverlay = findViewById(R.id.blur_overlay);

        setupTap(contentContainer);
        setupBackHandler();
    }

    /* ========================================================= */
    /* ================= WINDOW ================================ */
    /* ========================================================= */

    private void prepareWindow() {
        Window window = getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());

        controller.hide(android.view.WindowInsets.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /* ========================================================= */
    /* ================= BACK ================================ */
    /* ========================================================= */

    private void setupBackHandler() {
        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                if (intentBar != null) {
                                    hideIntentBar();
                                }
                            }
                        });
    }

    /* ========================================================= */
    /* ================= TAP ================================ */
    /* ========================================================= */

    private void setupTap(View view) {
        view.setOnClickListener(v -> showIntentBar());
    }

    /* ========================================================= */
    /* ================= INTENT BAR ============================ */
    /* ========================================================= */

    private void showIntentBar() {

        if (intentBar != null || isAnimating) return;

        isAnimating = true;

        //  blur ONLY background
        applyBlur(10f);

        //  dim overlay
        blurOverlay.setBackgroundColor(0x33000000);
        blurOverlay.setAlpha(0f);
        blurOverlay.setVisibility(View.VISIBLE);
        blurOverlay.animate().alpha(1f).setDuration(150).start();

        intentBar =
                new IntentBarView(
                        this,
                        new IntentBarView.Callback() {
                            @Override
                            public void onIntentEntered(String text) {
                                hideIntentBar();
                                launcherController.handleIntentText(text);
                            }

                            @Override
                            public void onDismiss() {
                                hideIntentBar();
                            }

                            @Override
                            public void onSettingsClick() {
                                hideIntentBar();
                                openSettings();
                            }
                        });

        // swipe to dismiss
        intentBar.setOnTouchListener(
                (v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downY = event.getY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float delta = event.getY() - downY;

                            if (delta > dp(80)) {
                                hideIntentBar();
                                return true;
                            }
                            return true;
                    }
                    return false;
                });

        //  ADD TO OVERLAY (NOT ROOT)
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT //  FIX
                        );
        params.gravity = Gravity.BOTTOM;

        overlayContainer.addView(intentBar, params);

        // animation
        intentBar.setTranslationY(dp(80));
        intentBar.setAlpha(0f);

        intentBar
                .animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(180)
                .withEndAction(() -> isAnimating = false)
                .start();
    }

    private void hideIntentBar() {

        if (intentBar == null || isAnimating) return;

        isAnimating = true;

        IntentBarView bar = intentBar;
        intentBar = null;

        bar.animate()
                .translationY(dp(80))
                .alpha(0f)
                .setDuration(150)
                .withEndAction(
                        () -> {
                            overlayContainer.removeView(bar); // FIXED
                            isAnimating = false;
                        })
                .start();

        blurOverlay
                .animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(
                        () -> {
                            blurOverlay.setVisibility(View.GONE);
                            clearBlur();
                        })
                .start();
    }

    /* ========================================================= */
    /* ================= SETTINGS =============================== */
    /* ========================================================= */

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /* ========================================================= */
    /* ================= BLUR ================================ */
    /* ========================================================= */

    private void applyBlur(float radius) {
        if (Build.VERSION.SDK_INT >= 31 && contentContainer != null) {
            contentContainer.setRenderEffect(
                    RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
        }
    }

    private void clearBlur() {
        if (Build.VERSION.SDK_INT >= 31 && contentContainer != null) {
            contentContainer.setRenderEffect(null);
        }
    }

    /* ========================================================= */
    /* ================= UTIL ================================ */
    /* ========================================================= */

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
