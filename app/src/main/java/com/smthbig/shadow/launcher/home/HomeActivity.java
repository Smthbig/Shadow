package com.smthbig.shadow.launcher.home;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

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

        if (binding.doomsday != null) binding.doomsday.updateState();
        binding.settingsBtn.setOnClickListener(v -> openSettings());
        binding.contentContainer.setOnClickListener(v -> showSearch());
        binding.blurOverlay.setOnClickListener(v -> hideSearch());

        setupIntentBar();
        setupBackHandler();
        setupKeyboardAnimation();
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
        if (binding.doomsday != null) binding.doomsday.updateState();
    }

    private void setupIntentBar() {
        if (binding.intentBar == null) return;
        binding.intentBar.setCallback(new IntentBarView.Callback() {
            @Override
            public void onIntentEntered(String text) {
                Log.d(TAG, "Intent entered: " + text);
                hideSearch();
                viewModel.handleIntent(text);
            }

            @Override
            public void onDismiss() {
                hideSearch();
            }
        });
    }

    private void showSearch() {
        if (isAnimating || binding.intentBar.getVisibility() == View.VISIBLE) return;
        isAnimating = true;
        viewModel.setSearchVisible(true);

        binding.contentContainer.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

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
