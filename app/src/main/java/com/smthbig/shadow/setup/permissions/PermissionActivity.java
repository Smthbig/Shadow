package com.smthbig.shadow.setup.permissions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.smthbig.shadow.databinding.ActivityPermissionBinding;
import com.smthbig.shadow.launcher.home.HomeActivity;
import com.smthbig.shadow.theme.ThemeManager;

public final class PermissionActivity extends AppCompatActivity {

    private static final String TAG = "PermissionAct";
    private ActivityPermissionBinding binding;

    private final ActivityResultLauncher<Intent> roleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d(TAG, "Role request returned, re-checking");
                        updateUI();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeManager.applyWallpaper(this);

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        boolean hasUsage = UsagePermissionHelper.hasUsageAccess(this);
        boolean isHome = UsagePermissionHelper.isDefaultHomeApp(this);

        if (!hasUsage) {
            showUsageStep();
        } else if (!isHome) {
            showHomeStep();
        } else {
            goHome();
        }
    }

    private void showUsageStep() {
        binding.statusText.setText(
                "Usage access required.\n\n" +
                "Enable 'Allow restricted settings' in App Info if toggle is disabled.");
        binding.actionButton.setText("Open Usage Access");
        binding.actionButton.setOnClickListener(v ->
                startActivity(UsagePermissionHelper.getUsageAccessIntent()));
        binding.skipButton.setVisibility(View.VISIBLE);
        binding.skipButton.setText("Open App Info");
        binding.skipButton.setOnClickListener(v ->
                startActivity(UsagePermissionHelper.getAppDetailsIntent(this)));
    }

    private void showHomeStep() {
        binding.statusText.setText(
                "Set Shadow as default launcher.\n\n" +
                "This step is mandatory for full functionality.");
        binding.actionButton.setText("Set Default Launcher");
        binding.actionButton.setOnClickListener(v -> requestHomeRole());
        binding.skipButton.setVisibility(View.GONE);
    }

    private void requestHomeRole() {
        Intent intent = UsagePermissionHelper.getHomeRoleRequestIntent(this);
        if (intent != null) {
            try {
                roleLauncher.launch(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch role request", e);
                startActivity(intent);
            }
        }
    }

    private void goHome() {
        Log.d(TAG, "All permissions granted, navigating to home");
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}