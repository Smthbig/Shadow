package com.smthbig.shadow.delay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.smthbig.shadow.R;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.theme.ThemeManager;

import java.util.concurrent.TimeUnit;

public final class DelayOverlayActivity extends AppCompatActivity {

    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_DELAY = "delay";
    private static final String EXTRA_REASON = "reason";
    private static final String EXTRA_PACKAGE = "pkg";
    private static final String EXTRA_EXTENSION = "extension";

    private static final int MODE_DELAY = 1;
    private static final int MODE_BLOCK = 2;

    private CountDownTimer timer;
    private boolean launched = false;

    /* ========================================================= */
    /* ================= FACTORY ================================ */
    /* ========================================================= */

    public static Intent delay(
            Context ctx,
            String pkg,
            long delayMs,
            String reason,
            boolean usingExtension
    ) {
        return base(ctx, pkg, reason, usingExtension)
                .putExtra(EXTRA_MODE, MODE_DELAY)
                .putExtra(EXTRA_DELAY, delayMs);
    }

    public static Intent block(
            Context ctx,
            String pkg,
            String reason
    ) {
        return base(ctx, pkg, reason, false)
                .putExtra(EXTRA_MODE, MODE_BLOCK);
    }

    private static Intent base(
            Context ctx,
            String pkg,
            String reason,
            boolean usingExtension
    ) {
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

        ThemeManager.apply(this); // ✅ ensure theme sync
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delay);

        int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_BLOCK);
        long delay = Math.max(0, getIntent().getLongExtra(EXTRA_DELAY, 0));
        String reason = getIntent().getStringExtra(EXTRA_REASON);
        boolean usingExtension =
                getIntent().getBooleanExtra(EXTRA_EXTENSION, false);

        String pkg = getIntent().getStringExtra(EXTRA_PACKAGE);

        if (pkg == null || pkg.isEmpty()) {
            finish();
            return;
        }

        /* ---------- VIEW BIND ---------- */

        TextView title = findViewById(R.id.title);
        TextView subtitle = findViewById(R.id.subtitle);
        TextView timerText = findViewById(R.id.timer);

        MaterialButton btnCancel = findViewById(R.id.btn_cancel);
        MaterialButton btnExtend = findViewById(R.id.btn_extend);

        CircularProgressIndicator progress =
                findViewById(R.id.progress);

        if (progress == null || timerText == null) {
            finish(); // safety
            return;
        }

        ExtensionEngine engine = new ExtensionEngine(this);

        /* ========================================================= */
        /* ================= BLOCK MODE ============================ */
        /* ========================================================= */

        if (mode == MODE_BLOCK) {

            title.setText("Blocked");
            subtitle.setText(reason != null ? reason : "Time limit reached");

            timerText.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            btnExtend.setVisibility(View.GONE);

            btnCancel.setText("Close");
            btnCancel.setOnClickListener(v -> finish());

            return;
        }

        /* ========================================================= */
        /* ================= DELAY MODE ============================ */
        /* ========================================================= */

        title.setText(usingExtension ? "Using Extension" : "Wait");
        subtitle.setText(reason != null ? reason : "");

        int safeMax = (int) Math.max(1, Math.min(delay, Integer.MAX_VALUE));
        progress.setMax(safeMax);
        progress.setProgress(0);

        timerText.setText((delay / 1000) + "s");

        btnCancel.setOnClickListener(v -> finish());

        /* ---------- EXTENSION BUTTON ---------- */

        if (usingExtension) {
            btnExtend.setVisibility(View.GONE);
        } else {

            btnExtend.setVisibility(View.VISIBLE);

            btnExtend.setOnClickListener(v -> {

                boolean granted =
                        engine.grant(pkg, TimeUnit.MINUTES.toMillis(5));

                if (granted) {
                    btnExtend.setText("Added ✓");
                    btnExtend.setEnabled(false);

                    btnExtend.performHapticFeedback(
                            HapticFeedbackConstants.CONFIRM
                    );
                } else {
                    btnExtend.setText("Limit reached");
                    btnExtend.setEnabled(false);
                }
            });
        }

        /* ========================================================= */
        /* ================= TIMER ================================ */
        /* ========================================================= */

        if (delay <= 0) {
            launchApp(pkg);
            finish();
            return;
        }

        timer = new CountDownTimer(delay, 50) {

            @Override
            public void onTick(long ms) {

                long elapsed = delay - ms;

                progress.setProgress((int) elapsed);
                timerText.setText((ms / 1000) + "s");
            }

            @Override
            public void onFinish() {

                if (launched) return;
                launched = true;

                timerText.setText("0s");
                progress.setProgress(progress.getMax());

                progress.performHapticFeedback(
                        HapticFeedbackConstants.CONFIRM
                );

                launchApp(pkg);
                finish();
            }

        }.start();
    }

    /* ========================================================= */
    /* ================= LAUNCH SAFE ============================ */
    /* ========================================================= */

    private void launchApp(String pkg) {
        try {
            Intent intent =
                    getPackageManager().getLaunchIntentForPackage(pkg);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception ignored) {}
    }

    /* ========================================================= */
    /* ================= SAFETY ================================= */
    /* ========================================================= */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }
    }
}