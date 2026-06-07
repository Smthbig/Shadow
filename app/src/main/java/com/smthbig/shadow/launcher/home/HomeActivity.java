package com.smthbig.shadow.launcher.home;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smthbig.shadow.databinding.ActivityHomeBinding;
import com.smthbig.shadow.launcher.core.LauncherController;
import com.smthbig.shadow.settings.SettingsActivity;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.theme.ThemeStore;
import com.smthbig.shadow.viewmodel.HomeViewModel;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private LauncherController launcherController;

    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        launcherController = viewModel.getLauncherController();

        prepareWindow();
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeManager.applyWallpaper(this);

        setupDateText();
        setupGreeting();
        setupIntention();
        setupTimer();
        setupStats();
        setupGoalProgress();
        setupContributionGrid();
        setupSearch();
        setupSettings();
        setupIntentBar();
        setupBackHandler();
        setupKeyboardAnimation();
    }

    private void setupDateText() {
        if (binding.dateLabel == null) return;
        viewModel.getDateText().observe(this, text -> {
            if (text != null) binding.dateLabel.setText(text);
        });
    }

    private void setupGreeting() {
        if (binding.greeting == null) return;
        viewModel.getGreeting().observe(this, text -> {
            if (text != null) binding.greeting.setText(text);
        });
    }

    private void setupIntention() {
        if (binding.intentionText == null) return;

        viewModel.getIntention().observe(this, text -> {
            if (text != null && !text.isEmpty()) {
                binding.intentionText.setText(text);
                binding.intentionText.setAlpha(0.9f);
            } else {
                binding.intentionText.setText("Set today's goal");
                binding.intentionText.setAlpha(0.35f);
            }
        });

        binding.intentionText.setOnClickListener(v -> showIntentionDialog());
    }

    private void showIntentionDialog() {
        EditText input = new EditText(this);
        String current = viewModel.getIntention().getValue();
        if (current != null) input.setText(current);
        input.setSelection(input.getText().length());
        input.setHint(getString(com.smthbig.shadow.R.string.goal_hint));

        new MaterialAlertDialogBuilder(this)
                .setTitle(com.smthbig.shadow.R.string.today_goal_label)
                .setView(input)
                .setPositiveButton(com.smthbig.shadow.R.string.set_intention, (d, w) -> {
                    String text = input.getText().toString().trim();
                    viewModel.setIntention(text);
                })
                .setNegativeButton(com.smthbig.shadow.R.string.clear_intention, (d, w) -> viewModel.setIntention(""))
                .show();
    }

    private void setupTimer() {
        if (binding.timerRow == null || binding.timerDisplay == null
                || binding.timerBtn == null) return;

        viewModel.getTimerRunning().observe(this, running -> {
            updateTimerButtonIcon(running);
            binding.timerLabel.setText(running ? "focusing..." : "focus");
        });

        viewModel.getTimerRemainingSecs().observe(this, secs -> {
            int mins = secs / 60;
            int sec = secs % 60;
            binding.timerDisplay.setText(String.format("%d:%02d", mins, sec));
        });

        binding.timerBtn.setOnClickListener(v -> {
            Boolean running = viewModel.getTimerRunning().getValue();
            if (running != null && running) {
                viewModel.pauseTimer();
                Toast.makeText(this, com.smthbig.shadow.R.string.focus_paused, Toast.LENGTH_SHORT).show();
            } else {
                if (viewModel.getTimerRemainingSecs().getValue() == null
                        || viewModel.getTimerRemainingSecs().getValue() <= 0) {
                    viewModel.setBaseMinutes(25);
                }
                viewModel.startTimer();
                Toast.makeText(this, com.smthbig.shadow.R.string.focus_started, Toast.LENGTH_SHORT).show();
            }
        });

        binding.timerDisplay.setOnLongClickListener(v -> {
            viewModel.resetTimer();
            Toast.makeText(this, com.smthbig.shadow.R.string.focus_reset, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void updateTimerButtonIcon(boolean running) {
        if (binding.timerBtn == null) return;
        Drawable icon;
        if (running) {
            icon = ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause);
        } else {
            icon = ContextCompat.getDrawable(this, android.R.drawable.ic_media_play);
        }
        binding.timerBtn.setImageDrawable(icon);
    }

    private void setupStats() {
        if (binding.statsRow == null) return;

        viewModel.getFocusMinutes().observe(this, mins -> {
            if (binding.statFocusValue != null) {
                binding.statFocusValue.setText(String.valueOf(mins));
            }
        });

        viewModel.getFocusSessions().observe(this, sessions -> {
            if (binding.statSessionsValue != null) {
                binding.statSessionsValue.setText(String.valueOf(sessions));
            }
        });

        viewModel.getBlocksToday().observe(this, blocks -> {
            if (binding.statBlocksValue != null) {
                binding.statBlocksValue.setText(String.valueOf(blocks));
            }
        });
    }

    private void setupGoalProgress() {
        if (binding.goalProgress == null || binding.goalProgressText == null
                || binding.goalMinutesLabel == null) return;

        viewModel.getGoalProgress().observe(this, progress -> {
            binding.goalProgress.setProgress(progress != null ? progress : 0);
        });

        viewModel.getFocusMinutes().observe(this, mins -> {
            Integer goal = viewModel.getDailyGoal().getValue();
            if (goal == null) goal = 120;
            int m = mins != null ? mins : 0;
            binding.goalProgressText.setText(String.format("%d / %d min", m, goal));
            binding.goalMinutesLabel.setText(String.format("%d%% of daily goal", goal > 0 ? (m * 100 / goal) : 0));
        });
    }

    private void setupContributionGrid() {
        if (binding.contributionGrid == null) return;
        binding.contributionGrid.updateData();
    }

    private void setupSearch() {
        if (binding.searchHint != null) {
            binding.searchHint.setOnClickListener(v -> showSearch());
        }
        binding.contentContainer.setOnClickListener(v -> showSearch());
    }

    private void setupSettings() {
        if (binding.settingsBtn == null) return;
        binding.settingsBtn.setOnClickListener(v -> openSettings());
    }

    private void setupKeyboardAnimation() {
        if (binding.intentBar == null) return;

        ViewCompat.setWindowInsetsAnimationCallback(binding.intentBar,
                new WindowInsetsAnimationCompat.Callback(
                        WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
                ) {
                    @Override
                    public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                    }

                    @NonNull
                    @Override
                    public WindowInsetsCompat onProgress(
                            @NonNull WindowInsetsCompat insets,
                            @NonNull List<WindowInsetsAnimationCompat> animations) {
                        int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                        int navHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                        binding.intentBar.setTranslationY(-Math.max(0, imeHeight - navHeight));
                        return insets;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeManager.applyWallpaper(this);
        viewModel.refreshStats();
        if (binding.contributionGrid != null) binding.contributionGrid.updateData();
    }

    private void setupIntentBar() {
        if (binding.intentBar == null) return;
        binding.intentBar.setCallback(new IntentBarView.Callback() {
            @Override
            public void onIntentEntered(String text) {
                Log.d(TAG, "Intent entered: " + text);
                hideSearch();
                if (isInFocusSession()) {
                    showFocusWarning(text);
                } else {
                    viewModel.handleIntent(text);
                }
            }

            @Override
            public void onDismiss() {
                hideSearch();
            }
        });
    }

    private boolean isInFocusSession() {
        Boolean running = viewModel.getTimerRunning().getValue();
        return running != null && running;
    }

    private void showFocusWarning(String query) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(com.smthbig.shadow.R.string.focus_warning_title)
                .setMessage(com.smthbig.shadow.R.string.focus_warning_message)
                .setPositiveButton(com.smthbig.shadow.R.string.launch_anyway, (d, w) -> viewModel.handleIntent(query))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showSearch() {
        if (isAnimating || binding.intentBar.getVisibility() == View.VISIBLE) return;
        isAnimating = true;
        viewModel.setSearchVisible(true);

        binding.getRoot().performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

        applyBlur(12f);
        binding.intentBar.setVisibility(View.VISIBLE);
        binding.intentBar.setAlpha(0f);
        binding.blurOverlay.setVisibility(View.VISIBLE);
        binding.blurOverlay.setAlpha(0f);

        binding.blurOverlay.animate().alpha(1f).setDuration(250).start();
        binding.intentBar.animate()
                .alpha(1f)
                .setDuration(300)
                .withEndAction(() -> {
                    isAnimating = false;
                    binding.intentBar.focus();
                })
                .start();
    }

    private void hideSearch() {
        if (isAnimating || binding.intentBar.getVisibility() != View.VISIBLE) return;
        isAnimating = true;
        viewModel.setSearchVisible(false);

        binding.intentBar.clearFocusAndHide();
        binding.blurOverlay.animate().alpha(0f).setDuration(200).start();
        binding.intentBar.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction(() -> {
                    binding.intentBar.setVisibility(View.GONE);
                    binding.blurOverlay.setVisibility(View.GONE);
                    clearBlur();
                    isAnimating = false;
                })
                .start();
    }

    private void prepareWindow() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(!ThemeStore.isDark(this));
        controller.setAppearanceLightNavigationBars(!ThemeStore.isDark(this));
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.intentBar.getVisibility() == View.VISIBLE) {
                    hideSearch();
                } else {
                    finish();
                }
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void applyBlur(float radius) {
        if (Build.VERSION.SDK_INT >= 31) {
            binding.contentContainer.setRenderEffect(
                    RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
        }
    }

    private void clearBlur() {
        if (Build.VERSION.SDK_INT >= 31) {
            binding.contentContainer.setRenderEffect(null);
        }
    }
}
