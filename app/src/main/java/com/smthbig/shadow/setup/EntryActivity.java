package com.smthbig.shadow.setup;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.smthbig.shadow.setup.permissions.PermissionActivity;
import com.smthbig.shadow.theme.ThemeManager;



public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        if (!SetupManager.isSetupDone(this)) {
            startActivity(new Intent(this, SetupActivity.class));
        } else {
            // After setup → go to permissions (or launcher if already granted)
            startActivity(new Intent(this, PermissionActivity.class));
        }

        finish();
    }
}