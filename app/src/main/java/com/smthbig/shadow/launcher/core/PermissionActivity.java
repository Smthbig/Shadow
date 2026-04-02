package com.smthbig.shadow.launcher.core;

import android.app.role.RoleManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smthbig.shadow.R;
import com.smthbig.shadow.system.UsagePermissionHelper;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.launcher.home.HomeActivity;

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

        statusText = findViewById(R.id.status_text);
        actionButton = findViewById(R.id.action_button);
        skipButton = findViewById(R.id.skip_button);

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
                "If restricted settings are blocked,\n" +
                "you can skip and configure later."
        );

        actionButton.setText("Open App Settings");

        actionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });

        // ✅ Allow skip ONLY here
        skipButton.setVisibility(View.VISIBLE);
        skipButton.setOnClickListener(v -> {
            // Move forward but DO NOT bypass launcher requirement
            showHomeStep();
        });
    }

    /* ========================================================= */
    /* ================= HOME STEP ============================= */
    /* ========================================================= */

    private void showHomeStep() {

        statusText.setText(
                "Set Shadow as default launcher.\n\n" +
                "This step is required to continue."
        );

        actionButton.setText("Set Default Launcher");

        actionButton.setOnClickListener(v -> requestHomeRole());

        // ❌ No skip allowed here (critical enforcement)
        skipButton.setVisibility(View.GONE);
    }

    private void requestHomeRole() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);

            if (roleManager != null
                    && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)
                    && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {

                roleLauncher.launch(
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                );
                return;
            }
        }

        // fallback
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
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