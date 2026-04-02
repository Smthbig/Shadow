package com.smthbig.shadow.delay;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.smthbig.shadow.R;
import com.smthbig.shadow.extension.ExtensionGrantActivity;
import com.smthbig.shadow.theme.ThemeManager;

public final class DelayOverlayActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_INTENT = "extra_target";
    public static final String EXTRA_DELAY_MS = "extra_delay";
    public static final String EXTRA_REASON = "extra_reason";
    public static final String EXTRA_USING_EXTENSION = "extra_using_extension";
    public static final String EXTRA_BLOCK = "extra_block";

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Intent target;
    private long endTime;

    private boolean usingExtension;
    private boolean launched;

    private android.widget.TextView text;
    private android.widget.Button extendBtn;

    /* ---------- LIFECYCLE ---------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        prepareWindow();
        setContentView(R.layout.activity_delay_overlay);

        text = findViewById(R.id.text);
        extendBtn = findViewById(R.id.btn_extend);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        boolean isBlocked = intent.getBooleanExtra(EXTRA_BLOCK, false);
        String reason = intent.getStringExtra(EXTRA_REASON);

        if (Build.VERSION.SDK_INT >= 33) {
            target = intent.getParcelableExtra(EXTRA_TARGET_INTENT, Intent.class);
        } else {
            target = intent.getParcelableExtra(EXTRA_TARGET_INTENT);
        }

        long delay = Math.max(0, intent.getLongExtra(EXTRA_DELAY_MS, 0));
        usingExtension = intent.getBooleanExtra(EXTRA_USING_EXTENSION, false);

        if (isBlocked) {
            handleBlock(reason);
            return;
        }

        if (target == null) {
            finish();
            return;
        }

        if (delay == 0) {
            launchSafely();
            return;
        }

        endTime = System.currentTimeMillis() + delay;

        updateText(delay);
        handler.post(tickRunnable);
    }

    /* ---------- INTENT BUILDERS (RESTORE) ---------- */

    public static Intent delay(
            Context context, Intent target, long delayMs, String reason, boolean usingExtension) {
        Intent i = new Intent(context, DelayOverlayActivity.class);

        i.putExtra(EXTRA_TARGET_INTENT, target);
        i.putExtra(EXTRA_DELAY_MS, Math.max(0, delayMs));
        i.putExtra(EXTRA_REASON, reason);
        i.putExtra(EXTRA_USING_EXTENSION, usingExtension);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return i;
    }

    public static Intent block(Context context, Intent target, String reason) {
        Intent i = new Intent(context, DelayOverlayActivity.class);

        i.putExtra(EXTRA_BLOCK, true);
        i.putExtra(EXTRA_REASON, reason);
        i.putExtra(EXTRA_TARGET_INTENT, target);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return i;
    }

    /* ---------- TIMER ---------- */

    private final Runnable tickRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (launched) return;

                    long remaining = endTime - System.currentTimeMillis();

                    if (remaining <= 0) {
                        launchSafely();
                        return;
                    }

                    updateText(remaining);
                    handler.postDelayed(this, 1000);
                }
            };

    /* ---------- UI ---------- */

    private void handleBlock(String reason) {

        text.setText(reason != null ? reason : "Daily limit reached");

        if (target != null && target.getComponent() != null) {
            extendBtn.setVisibility(android.view.View.VISIBLE);

            extendBtn.setOnClickListener(
                    v -> {
                        Intent i = new Intent(this, ExtensionGrantActivity.class);
                        i.putExtra("pkg", target.getComponent().getPackageName());
                        startActivity(i);
                        finish();
                    });
        }
    }

    private void updateText(long remainingMs) {

        int seconds = (int) Math.ceil(remainingMs / 1000f);

        String reason = getIntent().getStringExtra(EXTRA_REASON);

        StringBuilder sb = new StringBuilder();

        if (reason != null) {
            sb.append(reason).append("\n\n");
        }

        if (usingExtension) {
            sb.append("Using extension time\n\n");
        }

        sb.append("Continuing in ").append(seconds).append("s");

        text.setText(sb.toString());
    }

    /* ---------- NAVIGATION ---------- */

    private void launchSafely() {
        if (launched) return;
        launched = true;

        try {
            Intent launch = new Intent(target);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception ignored) {
        } finally {
            finish();
        }
    }

    /* ---------- WINDOW (MODERN) ---------- */

    private void prepareWindow() {
        Window window = getWindow();

        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
