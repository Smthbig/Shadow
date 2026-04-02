package com.smthbig.shadow.launcher.core;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smthbig.shadow.R;
import com.smthbig.shadow.system.UsagePermissionHelper;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.launcher.home.HomeActivity;

public final class PermissionActivity extends AppCompatActivity {

    private static final int REQ_HOME_ROLE = 1001;

    private MaterialTextView statusText;
    private MaterialButton actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this); // 🔥 apply theme first
        super.onCreate(savedInstanceState);

        if (UsagePermissionHelper.hasRequiredPermissions(this)) {
            goHome();
            return;
        }

        setContentView(R.layout.activity_permission);

        statusText = findViewById(R.id.status_text);
        actionButton = findViewById(R.id.action_button);

        updateUI();
    }

    private void updateUI() {
        boolean hasUsage = UsagePermissionHelper.hasUsageAccess(this);
        boolean isHome = UsagePermissionHelper.isDefaultHomeApp(this);

        if (!hasUsage) {
            setupUsageStep();
        } else if (!isHome) {
            setupHomeStep();
        } else {
            goHome();
        }
    }

    /* ---------- USAGE ACCESS ---------- */

    private void setupUsageStep() {
        statusText.setText(
                "Grant App Usage Access.\n\n" +
                "Steps:\n" +
                "1. Open App Info\n" +
                "2. Allow restricted settings\n" +
                "3. Enable Usage Access"
        );

        actionButton.setText("Open App Info");
        actionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });
    }

    /* ---------- HOME ROLE ---------- */

    private void setupHomeStep() {
        statusText.setText(
                "Set Shadow as default launcher.\n\n" +
                "Required to control app launches."
        );

        actionButton.setText("Set Default Launcher");
        actionButton.setOnClickListener(v -> requestHomeRole());
    }

    private void requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);

            if (roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {

                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
                startActivityForResult(intent, REQ_HOME_ROLE);
                return;
            }
        }

        // fallback
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    /* ---------- NAVIGATION ---------- */

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        startActivity(intent);
        finish();
    }
}