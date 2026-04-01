package com.smthbig.shadow.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.smthbig.shadow.R;

public class HomeActivity extends Activity {

    private LauncherController launcherController;

    private IntentBarView intentBar;
    private ImageView settingsButton;

    private View blurOverlay;
    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareWindow();

        launcherController = new LauncherController(this);

        setContentView(R.layout.activity_home);

        FrameLayout root = findViewById(R.id.root);
        blurOverlay = findViewById(R.id.blur_overlay);

        setupTap(root);
    }

    @Override
    public void onBackPressed() {
        // block back
    }

    private void prepareWindow() {
        Window window = getWindow();

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    /* ---------- TAP ---------- */

    private void setupTap(View view) {
        view.setOnClickListener(v -> showIntentBar());
    }

    /* ---------- INTENT BAR ---------- */

    private void showIntentBar() {
        if (intentBar != null) return;

        blurOverlay.setVisibility(View.VISIBLE);

        FrameLayout container = new FrameLayout(this);

        intentBar = new IntentBarView(
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
                }
        );

        // swipe down to dismiss
        intentBar.setOnTouchListener((v, event) -> {
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

        container.addView(intentBar);

        /* ---------- SETTINGS BUTTON ---------- */

        settingsButton = new ImageView(this);
        settingsButton.setImageResource(R.drawable.ic_settings_gear);
        settingsButton.setAlpha(0.9f);

        settingsButton.setOnClickListener(v -> openAppLimit());

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(dp(32), dp(32));

        params.gravity = Gravity.TOP | Gravity.END;
        params.topMargin = dp(24);
        params.rightMargin = dp(16);

        container.addView(settingsButton, params);

        addContentView(
                container,
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                )
        );
    }

    private void hideIntentBar() {
        if (intentBar != null) {
            View parent = (View) intentBar.getParent();

            if (parent instanceof FrameLayout) {
                ((FrameLayout) parent.getParent()).removeView(parent);
            }

            intentBar = null;
            settingsButton = null;
        }

        blurOverlay.setVisibility(View.GONE);
    }

    /* ---------- SETTINGS ---------- */

    private void openAppLimit() {
        try {
            startActivity(new Intent(this, AppLimitActivity.class));
        } catch (Exception ignored) {}
    }

    /* ---------- UTIL ---------- */

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}