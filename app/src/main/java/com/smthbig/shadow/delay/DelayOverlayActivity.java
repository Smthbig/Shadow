package com.smthbig.shadow.delay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class DelayOverlayActivity extends Activity {

    public static final String EXTRA_TARGET_INTENT = "extra_target";
    public static final String EXTRA_DELAY_MS = "extra_delay";
    public static final String EXTRA_REASON = "extra_reason";
    public static final String EXTRA_USING_EXTENSION = "extra_using_extension";
    public static final String EXTRA_BLOCK = "extra_block";

    /* ---------- Intent builders ---------- */

    // ✅ NEW: backward-compatible overload
    public static Intent delay(
            Context context,
            Intent target,
            long delayMs,
            String reason
    ) {
        return delay(
                context,
                target,
                delayMs,
                reason,
                false
        );
    }

    public static Intent delay(
            Context context,
            Intent target,
            long delayMs,
            String reason,
            boolean usingExtension
    ) {
        Intent i = new Intent(context, DelayOverlayActivity.class);
        i.putExtra(EXTRA_TARGET_INTENT, target);
        i.putExtra(EXTRA_DELAY_MS, Math.max(0, delayMs));
        i.putExtra(EXTRA_REASON, reason);
        i.putExtra(EXTRA_USING_EXTENSION, usingExtension);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    public static Intent block(
            Context context,
            String reason
    ) {
        Intent i = new Intent(context, DelayOverlayActivity.class);
        i.putExtra(EXTRA_BLOCK, true);
        i.putExtra(EXTRA_REASON, reason);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    /* ---------- State ---------- */

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Intent target;
    private long remainingMs;
    private boolean usingExtension;
    private boolean launched;

    private TextView text;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (launched) return;

            remainingMs -= 1000;

            if (remainingMs <= 0) {
                launchSafely();
                return;
            }

            updateText();
            handler.postDelayed(this, 1000);
        }
    };

    /* ---------- Lifecycle ---------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareWindow();

        Intent intent = getIntent();

        boolean isBlocked =
                intent.getBooleanExtra(EXTRA_BLOCK, false);

        String reason =
                intent.getStringExtra(EXTRA_REASON);

        if (isBlocked) {
            setupUI();
            text.setText(
                    reason != null
                            ? reason
                            : "Daily limit reached"
            );
            return;
        }

        target =
                intent.getParcelableExtra(EXTRA_TARGET_INTENT);

        remainingMs =
                Math.max(
                        0,
                        intent.getLongExtra(EXTRA_DELAY_MS, 0)
                );

        usingExtension =
                intent.getBooleanExtra(
                        EXTRA_USING_EXTENSION,
                        false
                );

        if (target == null) {
            finish();
            return;
        }

        if (remainingMs == 0) {
            launchSafely();
            return;
        }

        setupUI();
        updateText();
        handler.postDelayed(tickRunnable, 1000);
    }

    /* ---------- UI ---------- */

    private void setupUI() {
        FrameLayout root = new FrameLayout(this);

        GradientDrawable bg =
                new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                                Color.parseColor("#0E0E0E"),
                                Color.parseColor("#1A1A1A"),
                                Color.parseColor("#121212")
                        }
                );

        root.setBackground(bg);

        text = new TextView(this);
        text.setTextColor(Color.parseColor("#EDEDED"));
        text.setTextSize(18f);
        text.setGravity(Gravity.CENTER);
        text.setLineSpacing(0f, 1.3f);

        root.addView(
                text,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(root);
    }

    private void updateText() {
        String reason =
                getIntent().getStringExtra(EXTRA_REASON);

        int seconds =
                (int) Math.ceil(remainingMs / 1000f);

        StringBuilder sb = new StringBuilder();

        if (reason != null && !reason.isEmpty()) {
            sb.append(reason).append("\n\n");
        }

        if (usingExtension) {
            sb.append("Using extension time\n\n");
        }

        sb.append("Continuing in ")
                .append(seconds)
                .append("s");

        text.setText(sb.toString());
    }

    /* ---------- Launch ---------- */

    private void launchSafely() {
        if (launched) return;
        launched = true;

        try {
            Intent launch = new Intent(target);
            launch.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );
            startActivity(launch);
        } catch (Exception ignored) {
        } finally {
            finish();
        }
    }

    /* ---------- Window ---------- */

    private void prepareWindow() {
        Window w = getWindow();
        w.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        w.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    public void onBackPressed() {
        // intentionally blocked
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}