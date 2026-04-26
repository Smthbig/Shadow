package com.smthbig.shadow.setup.permissions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smthbig.shadow.R;
import com.smthbig.shadow.launcher.home.HomeActivity;
import com.smthbig.shadow.theme.ThemeManager;

public final class PermissionActivity extends AppCompatActivity {

    private MaterialTextView statusText;
    private MaterialButton actionButton;
    private MaterialButton skipButton;

    private final ActivityResultLauncher<Intent> roleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> updateUI()
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permission);
        ThemeManager.applyWallpaper(this);

        statusText = findViewById(R.id.status_text);
        actionButton = findViewById(R.id.action_button);
        skipButton = findViewById(R.id.skip_button);

        if (statusText == null || actionButton == null || skipButton == null) {
            throw new RuntimeException("Permission layout binding failed");
        }

        updateUI();
    }

    /* ========================================================= */
    /* ================= STATE MACHINE ========================== */
    /* ========================================================= */

    private void updateUI() {

        boolean hasUsage = UsagePermissionHelper.hasUsageAccess(this);
        boolean isHome = UsagePermissionHelper.isDefaultHomeApp(this);

        if (!hasUsage) {
            showUsageStep();
            return;
        }

        if (!isHome) {
            showHomeStep();
            return;
        }

        goHome();
    }

    /* ========================================================= */
    /* ================= USAGE STEP ============================ */
    /* ========================================================= */

    private void showUsageStep() {

        statusText.setText(
                "Usage access required.\n\n" +
                "If toggle is disabled, enable 'Allow restricted settings' in App Info."
        );

        actionButton.setText("Open Usage Access");

        actionButton.setOnClickListener(v ->
                startActivity(UsagePermissionHelper.getUsageAccessIntent())
        );

        skipButton.setVisibility(View.VISIBLE);
        skipButton.setText("Open App Info");

        skipButton.setOnClickListener(v ->
                startActivity(UsagePermissionHelper.getAppDetailsIntent(this))
        );
    }

    /* ========================================================= */
    /* ================= HOME STEP ============================= */
    /* ========================================================= */

    private void showHomeStep() {

        statusText.setText(
                "Set Shadow as default launcher.\n\n" +
                "This step is mandatory."
        );

        actionButton.setText("Set Default Launcher");

        actionButton.setOnClickListener(v -> requestHomeRole());

        skipButton.setVisibility(View.GONE);
    }

    private void requestHomeRole() {

        Intent intent = UsagePermissionHelper.getHomeRoleRequestIntent(this);

        // If it's a role request → use launcher
        if (intent != null) {
            try {
                roleLauncher.launch(intent);
            } catch (Exception e) {
                // fallback safety
                startActivity(intent);
            }
        }
    }

    /* ========================================================= */
    /* ================= NAVIGATION ============================ */
    /* ========================================================= */

    private void goHome() {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    /* ========================================================= */
    /* ================= LIFECYCLE ============================= */
    /* ========================================================= */

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}