package com.smthbig.shadow.delay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.smthbig.shadow.R;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.theme.ThemeManager;

import java.util.concurrent.TimeUnit;

public final class DelayOverlayActivity extends AppCompatActivity {

    /* ================= EXTRAS ================= */

    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_DELAY = "delay";
    private static final String EXTRA_REASON = "reason";
    private static final String EXTRA_PACKAGE = "pkg";
    private static final String EXTRA_EXTENSION = "extension";

    private static final int MODE_DELAY = 1;
    private static final int MODE_BLOCK = 2;

    /* ================= STATE ================= */

    private CountDownTimer timer;
    private boolean launched = false;
    private long remainingMs = 0;

    /* ========================================================= */
    /* ================= FACTORY ================================ */
    /* ========================================================= */

    public static Intent delay(Context ctx, String pkg, long delayMs, String reason, boolean usingExtension) {
        return base(ctx, pkg, reason, usingExtension)
                .putExtra(EXTRA_MODE, MODE_DELAY)
                .putExtra(EXTRA_DELAY, delayMs);
    }

    public static Intent block(Context ctx, String pkg, String reason) {
        return base(ctx, pkg, reason, false)
                .putExtra(EXTRA_MODE, MODE_BLOCK);
    }

    private static Intent base(Context ctx, String pkg, String reason, boolean usingExtension) {
        Intent i = new Intent(ctx, DelayOverlayActivity.class);
        i.putExtra(EXTRA_PACKAGE, pkg);
        i.putExtra(EXTRA_REASON, reason);
        i.putExtra(EXTRA_EXTENSION, usingExtension);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    /* ========================================================= */
    /* ================= LIFECYCLE ============================== */
    /* ========================================================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState); 

        setContentView(R.layout.activity_delay);

        // 🔒 Block back
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // no-op
                    }
                });

        /* ================= INTENT ================= */

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        final int mode = intent.getIntExtra(EXTRA_MODE, MODE_BLOCK);
        final long delay = Math.max(0, intent.getLongExtra(EXTRA_DELAY, 0));
        final String reason = intent.getStringExtra(EXTRA_REASON);
        final boolean usingExtension = intent.getBooleanExtra(EXTRA_EXTENSION, false);
        final String pkg = intent.getStringExtra(EXTRA_PACKAGE);

        if (pkg == null || pkg.isEmpty()) {
            finish();
            return;
        }

        /* ================= VIEWS ================= */

        TextView title = findViewById(R.id.title);
        TextView subtitle = findViewById(R.id.subtitle);
        TextView timerText = findViewById(R.id.timer);

        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        MaterialButton btnExtend = findViewById(R.id.btn_extend);
        CircularProgressIndicator progress = findViewById(R.id.progress);

        if (title == null || subtitle == null || timerText == null ||
                btnCancel == null || btnExtend == null || progress == null) {
            finish();
            return;
        }

        ExtensionEngine engine = new ExtensionEngine(this);

        /* ===================================================== */
        /* ================= BLOCK MODE ========================== */
        /* ===================================================== */

        if (mode == MODE_BLOCK) {

            title.setText("Blocked");
            subtitle.setText(reason != null ? reason : "Daily limit reached");

            timerText.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            btnExtend.setVisibility(View.GONE);

            btnCancel.setText("Close");
            btnCancel.setOnClickListener(v -> finish());

            return;
        }

        /* ===================================================== */
        /* ================= DELAY MODE ========================== */
        /* ===================================================== */

        title.setText(usingExtension ? "Using Extension" : "Wait");
        subtitle.setText(reason != null ? reason : "");

        btnCancel.setOnClickListener(v -> finish());

        /* ---------- EXTENSION ---------- */

        if (usingExtension) {
            btnExtend.setVisibility(View.GONE);
        } else {

            btnExtend.setVisibility(View.VISIBLE);

            btnExtend.setOnClickListener(v -> {

                if (launched) return; // safety

                long extra = TimeUnit.MINUTES.toMillis(5);
                boolean granted = engine.grant(pkg, extra);

                if (granted) {

                    btnExtend.setEnabled(false);
                    btnExtend.setText("Added ✓");

                    btnExtend.performHapticFeedback(HapticFeedbackConstants.CONFIRM);

                    remainingMs += extra;

                    restartTimer(pkg, progress, timerText);

                } else {
                    btnExtend.setEnabled(false);
                    btnExtend.setText("Limit reached");
                }
            });
        }

        /* ---------- TIMER ---------- */

        if (delay <= 0) {
            launchOnce(pkg);
            return;
        }

        remainingMs = delay;
        startTimer(pkg, progress, timerText);
    }

    /* ========================================================= */
    /* ================= TIMER ================================ */
    /* ========================================================= */

    private void startTimer(String pkg, CircularProgressIndicator progress, TextView timerText) {

        int safeMax = (int) Math.min(remainingMs, Integer.MAX_VALUE);

        progress.setMax(safeMax);
        progress.setProgress(0);

        timer = new CountDownTimer(remainingMs, 50) {

            @Override
            public void onTick(long ms) {
                remainingMs = ms;

                int elapsed = (int) (safeMax - ms);
                progress.setProgress(Math.max(0, elapsed));

                timerText.setText((ms / 1000) + "s");
            }

            @Override
            public void onFinish() {
                launchOnce(pkg);
            }
        }.start();
    }

    private void restartTimer(String pkg, CircularProgressIndicator progress, TextView timerText) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        startTimer(pkg, progress, timerText);
    }

    /* ========================================================= */
    /* ================= SAFE LAUNCH ============================ */
    /* ========================================================= */

    private void launchOnce(String pkg) {

        if (launched) return;
        launched = true;

        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            }

        } catch (Exception ignored) {}

        finish();
    }

    /* ========================================================= */
    /* ================= CLEANUP ================================ */
    /* ========================================================= */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}