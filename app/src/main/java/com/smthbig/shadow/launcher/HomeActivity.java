package com.smthbig.shadow.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class HomeActivity extends Activity {

    // Controller (brain entry point)
    private LauncherController launcherController;

    // UI
    private IntentBarView intentBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareWindow();

        launcherController = new LauncherController(this);

        FrameLayout root = createRootView();
        setupTap(root);

        setContentView(root);
    }

    @Override
    public void onBackPressed() {
        // Launcher absorbs back
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

    private FrameLayout createRootView() {
        FrameLayout root = new FrameLayout(this);

        //  Background gradient (same family as DelayOverlay)
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#0E0E0E"), // left
                        Color.parseColor("#1A1A1A"), // center
                        Color.parseColor("#121212")  // right
                }
        );
        bg.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        root.setBackground(bg);

        //  Center seam (very subtle)
        View seam = new View(this);
        seam.setBackgroundColor(Color.WHITE);
        seam.setAlpha(0.05f);

        FrameLayout.LayoutParams seamParams =
                new FrameLayout.LayoutParams(
                        2,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
        seamParams.gravity = Gravity.CENTER_HORIZONTAL;

        root.addView(seam, seamParams);

        root.setClickable(true);
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        return root;
    }

    /** Tap anywhere → show intent bar */
    private void setupTap(View view) {
        view.setOnClickListener(v -> showIntentBar());
    }

    private void showIntentBar() {
        if (intentBar != null) return;

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

        addContentView(
                intentBar,
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                )
        );
    }

    private void hideIntentBar() {
        if (intentBar != null && intentBar.getParent() instanceof FrameLayout) {
            ((FrameLayout) intentBar.getParent()).removeView(intentBar);
            intentBar = null;
        }
    }
}