package com.smthbig.shadow.setup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.smthbig.shadow.R;
import com.smthbig.shadow.setup.permissions.PermissionActivity;
import com.smthbig.shadow.theme.ThemeManager;

public class SetupActivity extends AppCompatActivity {

    private CheckBox acceptCheckBox;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        acceptCheckBox = findViewById(R.id.checkbox_accept);
        continueButton = findViewById(R.id.button_continue);

        continueButton.setEnabled(false);

        acceptCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    continueButton.setEnabled(isChecked);
                });

        continueButton.setOnClickListener(
                v -> {
                    SetupManager.markSetupDone(this);

                    try {
                        Intent intent = new Intent(this, PermissionActivity.class);
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();

                        // fallback debug (very important)
                        android.widget.Toast.makeText(
                                        this,
                                        "PermissionActivity not found",
                                        android.widget.Toast.LENGTH_LONG)
                                .show();
                    }

                    finish();
                });
    }
}
