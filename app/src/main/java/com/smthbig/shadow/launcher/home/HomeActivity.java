package com.smthbig.shadow.launcher.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;

import com.smthbig.shadow.R;
import com.smthbig.shadow.settings.SettingsActivity;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.launcher.core.LauncherController;

public class HomeActivity extends AppCompatActivity {

    private LauncherController launcherController;

    private IntentBarView intentBar;
    private View blurOverlay;
    private View rootView;

    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        prepareWindow();

        launcherController = new LauncherController(this);

        setContentView(R.layout.activity_home);

        FrameLayout root = findViewById(R.id.root);
        rootView = findViewById(R.id.root);
        blurOverlay = findViewById(R.id.blur_overlay);

        setupTap(root);
    }

    @Override
    public void onBackPressed() {
        //  Proper handling (previously broken UX)
        if (intentBar != null) {
            hideIntentBar();
        }
    }

    /* ---------- WINDOW ---------- */

    private void prepareWindow() {
        Window window = getWindow();

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        window.getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /* ---------- TAP ---------- */

    private void setupTap(View view) {
        view.setOnClickListener(v -> showIntentBar());
    }

    /* ---------- INTENT BAR ---------- */

    private void showIntentBar() {
        if (intentBar != null) return;

        //  Apply blur to entire screen
        applyBlur(10f);

        // Optional dim overlay
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

        // Swipe dismiss
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

        // ADD DIRECTLY (NO FULLSCREEN CONTAINER)
        addContentView(
                intentBar,
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.BOTTOM));

        // Animation
        intentBar.setTranslationY(dp(80));
        intentBar.setAlpha(0f);
        intentBar.animate().translationY(0).alpha(1f).setDuration(180).start();
    }

    private void hideIntentBar() {
        if (intentBar == null) return;

        intentBar
                .animate()
                .translationY(dp(80))
                .alpha(0f)
                .setDuration(150)
                .withEndAction(
                        () -> {
                            ((FrameLayout) intentBar.getParent()).removeView(intentBar);
                            intentBar = null;
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

    /* ---------- SETTINGS ---------- */

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void applyBlur(float radius) {
        if (Build.VERSION.SDK_INT >= 31 && rootView != null) {

            rootView.setRenderEffect(
                    RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
        }
    }

    private void clearBlur() {
        if (Build.VERSION.SDK_INT >= 31 && rootView != null) {
            rootView.setRenderEffect(null);
        }
    }

    /* ---------- UTIL ---------- */

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
