package com.smthbig.shadow.setup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.smthbig.shadow.setup.permissions.PermissionActivity;
import com.smthbig.shadow.theme.ThemeManager;
import com.smthbig.shadow.tracking.UsageMonitorService;

public class EntryActivity extends AppCompatActivity {

    private static final String TAG = "Entry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        try {
            startService(new Intent(this, UsageMonitorService.class));
        } catch (Exception e) {
            Log.e(TAG, "Failed to start monitoring service", e);
        }

        if (!SetupManager.isSetupDone(this)) {
            startActivity(new Intent(this, SetupActivity.class));
        } else {
            startActivity(new Intent(this, PermissionActivity.class));
        }

        finish();
    }
}