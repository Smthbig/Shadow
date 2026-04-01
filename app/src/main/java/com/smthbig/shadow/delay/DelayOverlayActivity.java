package com.smthbig.shadow.delay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.smthbig.shadow.R;
import com.smthbig.shadow.extension.ExtensionGrantActivity;

public final class DelayOverlayActivity extends Activity {

    public static final String EXTRA_TARGET_INTENT = "extra_target";
    public static final String EXTRA_DELAY_MS = "extra_delay";
    public static final String EXTRA_REASON = "extra_reason";
    public static final String EXTRA_USING_EXTENSION = "extra_using_extension";
    public static final String EXTRA_BLOCK = "extra_block";

    /* ---------- Intent builders ---------- */

    public static Intent delay(
            Context context,
            Intent target,
            long delayMs,
            String reason
    ) {
        return delay(context, target, delayMs, reason, false);
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
            Intent target,
            String reason
    ) {
        Intent i = new Intent(context, DelayOverlayActivity.class);
        i.putExtra(EXTRA_BLOCK, true);
        i.putExtra(EXTRA_REASON, reason);
        i.putExtra(EXTRA_TARGET_INTENT, target);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    /* ---------- State ---------- */

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Intent target;
    private long remainingMs;
    private long endTime;

    private boolean usingExtension;
    private boolean launched;

    private TextView text;
    private Button extendBtn;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (launched) return;

            long now = System.currentTimeMillis();
            long remaining = endTime - now;

            if (remaining <= 0) {
                launchSafely();
                return;
            }

            updateText(remaining);
            handler.postDelayed(this, 1000);
        }
    };

    /* ---------- Lifecycle ---------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareWindow();

        setContentView(R.layout.activity_delay_overlay);

        text = findViewById(R.id.text);
        extendBtn = findViewById(R.id.btn_extend);

        findViewById(R.id.root)
                .setBackgroundResource(R.drawable.bg_shadow_gradient);

        Intent intent = getIntent();

        boolean isBlocked =
                intent.getBooleanExtra(EXTRA_BLOCK, false);

        String reason =
                intent.getStringExtra(EXTRA_REASON);

        if (Build.VERSION.SDK_INT >= 33) {
            target = intent.getParcelableExtra(EXTRA_TARGET_INTENT, Intent.class);
        } else {
            target = intent.getParcelableExtra(EXTRA_TARGET_INTENT);
        }

        remainingMs = Math.max(
                0,
                intent.getLongExtra(EXTRA_DELAY_MS, 0)
        );

        usingExtension =
                intent.getBooleanExtra(EXTRA_USING_EXTENSION, false);

        if (isBlocked) {
            handleBlock(reason);
            return;
        }

        if (target == null) {
            finish();
            return;
        }

        if (remainingMs == 0) {
            launchSafely();
            return;
        }

        endTime = System.currentTimeMillis() + remainingMs;

        updateText(remainingMs);
        handler.postDelayed(tickRunnable, 1000);
    }

    private void handleBlock(String reason) {
        text.setText(
                reason != null
                        ? reason
                        : "Daily limit reached"
        );

        if (target != null && target.getComponent() != null) {
            extendBtn.setVisibility(View.VISIBLE);

            extendBtn.setOnClickListener(v -> {
                Intent i = new Intent(this, ExtensionGrantActivity.class);
                i.putExtra(
                        "pkg",
                        target.getComponent().getPackageName()
                );
                startActivity(i);
                finish();
            });
        }
    }

    private void updateText(long remainingMs) {
        int seconds = (int) Math.ceil(remainingMs / 1000f);

        StringBuilder sb = new StringBuilder();

        String reason =
                getIntent().getStringExtra(EXTRA_REASON);

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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}