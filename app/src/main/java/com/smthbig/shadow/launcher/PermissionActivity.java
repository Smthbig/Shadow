package com.smthbig.shadow.launcher;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smthbig.shadow.R;
import com.smthbig.shadow.system.UsagePermissionHelper;

public final class PermissionActivity extends Activity {

    private static final int REQ_HOME_ROLE = 1001;

    private TextView statusText;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UsagePermissionHelper.hasRequiredPermissions(this)) {
            goHome();
            return;
        }

        setContentView(createContent());
        updateUI();
    }

    private View createContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundResource(R.drawable.bg_shadow_gradient);

        statusText = new TextView(this);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(16f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setLineSpacing(0f, 1.3f);

        actionButton = new Button(this);

        root.addView(statusText);
        root.addView(actionButton);

        return root;
    }

    private void updateUI() {
        boolean hasUsage = UsagePermissionHelper.hasUsageAccess(this);
        boolean isHome = UsagePermissionHelper.isDefaultHomeApp(this);

        if (!hasUsage) {
            showUsagePermissionStep();
            return;
        }

        if (!isHome) {
            showHomePermissionStep();
            return;
        }

        goHome();
    }

    /* ---------- Usage permission ---------- */

    private void showUsagePermissionStep() {
        statusText.setText(
                "Shadow needs App Usage Access.\n\n" +
                "On Android 12+, this is a restricted permission.\n\n" +
                "Steps:\n" +
                "1. Open App Info\n" +
                "2. Tap ⋮ (top right)\n" +
                "3. Allow restricted settings\n" +
                "4. Go back and enable Usage Access"
        );

        actionButton.setText("Open App Info");
        actionButton.setOnClickListener(v -> openAppInfo());
    }

    private void openAppInfo() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        );
        intent.setData(
                Uri.fromParts("package", getPackageName(), null)
        );
        startActivity(intent);
    }

    /* ---------- Home / Launcher permission ---------- */

    private void showHomePermissionStep() {
        statusText.setText(
                "Shadow must be set as your default launcher.\n\n" +
                "This allows Shadow to control app launches."
        );

        actionButton.setText("Set as Default Home");
        actionButton.setOnClickListener(v -> requestHomeRole());
    }

    private void requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager =
                    (RoleManager) getSystemService(ROLE_SERVICE);

            if (roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {

                Intent intent =
                        roleManager.createRequestRoleIntent(
                                RoleManager.ROLE_HOME
                        );
                startActivityForResult(intent, REQ_HOME_ROLE);
                return;
            }
        }

        // OEM / legacy fallback
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        startActivity(intent);
        finish();
    }
}