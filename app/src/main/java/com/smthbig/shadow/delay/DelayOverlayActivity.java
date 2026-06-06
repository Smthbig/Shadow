package com.smthbig.shadow.delay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.smthbig.shadow.databinding.ActivityDelayBinding;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.tracking.FrictionStore;
import com.smthbig.shadow.viewmodel.DelayViewModel;

public final class DelayOverlayActivity extends AppCompatActivity {

    private static final String TAG = "DelayOverlay";

    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_DELAY = "delay";
    private static final String EXTRA_REASON = "reason";
    private static final String EXTRA_PACKAGE = "pkg";
    private static final String EXTRA_EXTENSION_GRANTED = "extension_granted";

    private static final int MODE_DELAY = 1;
    private static final int MODE_BLOCK = 2;
    private static final long EXTENSION_MINUTES = 5;

    private ActivityDelayBinding binding;
    private DelayViewModel viewModel;
    private CountDownTimer timer;
    private boolean launched = false;
    private String pkg;

    public static Intent delay(Context ctx, String pkg, long delayMs, String reason, boolean usingExtension) {
        return base(ctx, pkg, reason)
                .putExtra(EXTRA_MODE, MODE_DELAY)
                .putExtra(EXTRA_DELAY, delayMs);
    }

    public static Intent block(Context ctx, String pkg, String reason) {
        return base(ctx, pkg, reason).putExtra(EXTRA_MODE, MODE_BLOCK);
    }

    private static Intent base(Context ctx, String pkg, String reason) {
        Intent i = new Intent(ctx, DelayOverlayActivity.class);
        i.putExtra(EXTRA_PACKAGE, pkg);
        i.putExtra(EXTRA_REASON, reason);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DelayViewModel.class);
        binding = ActivityDelayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeManager.applyWallpaper(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { }
        });

        Intent intent = getIntent();
        if (intent == null) { finish(); return; }

        final int mode = intent.getIntExtra(EXTRA_MODE, MODE_BLOCK);
        final long delay = Math.max(0, intent.getLongExtra(EXTRA_DELAY, 0));
        final String reason = intent.getStringExtra(EXTRA_REASON);
        pkg = intent.getStringExtra(EXTRA_PACKAGE);
        long additionalExtension = intent.getLongExtra(EXTRA_EXTENSION_GRANTED, 0);

        if (pkg == null || pkg.isEmpty()) { finish(); return; }

        Log.d(TAG, "Delay for " + pkg + ": " + delay + "ms, reason: " + reason);

        viewModel.initialize(pkg, delay, additionalExtension);

        binding.title.setText("Shadow Friction");
        binding.subtitle.setText(viewModel.getQuote().getValue());
        binding.btnCancel.setOnClickListener(v -> finish());

        setupExtendButton();

        if (mode == MODE_BLOCK || delay <= 0) {
            binding.timer.setText(mode == MODE_BLOCK ? "Blocked" : "0s");
            if (delay <= 0) launchOnce(pkg);
            return;
        }

        startTimer();
    }

    private void setupExtendButton() {
        binding.btnExtend.setVisibility(View.VISIBLE);
        binding.btnExtend.setOnClickListener(v -> {
            if (launched) return;

            boolean granted = viewModel.grantExtension();
            if (granted) {
                Log.d(TAG, "Extension granted for " + pkg);
                binding.btnExtend.setText("+" + EXTENSION_MINUTES + "m added");
                binding.btnExtend.setEnabled(false);
                binding.btnExtend.performHapticFeedback(HapticFeedbackConstants.CONFIRM);

                if (timer != null) timer.cancel();
                startTimer();
            } else {
                android.widget.Toast.makeText(this,
                        "Daily extension limit reached", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        long totalMs = viewModel.getTotalMs();
        if (totalMs <= 0) {
            launchOnce(pkg);
            return;
        }

        int safeMax = totalMs > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalMs;
        binding.progress.setMax(Math.max(safeMax, 1));
        binding.progress.setProgress(0);

        timer = new CountDownTimer(totalMs, 100) {
            @Override
            public void onTick(long ms) {
                if (launched) return;
                viewModel.tick(ms);

                int elapsed = safeMax - (int) ms;
                binding.progress.setProgress(Math.max(0, Math.min(elapsed, safeMax)));
                binding.timer.setText(String.valueOf(Math.max(1, (ms / 1000))) + "s");
            }

            @Override
            public void onFinish() {
                if (!launched) launchOnce(pkg);
            }
        }.start();
    }

    private void launchOnce(String pkg) {
        if (launched) return;
        launched = true;

        Log.d(TAG, "Launching " + pkg + " after delay");

        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch " + pkg, e);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FrictionStore.getInstance().clearActiveDelay();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
