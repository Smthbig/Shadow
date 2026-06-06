package com.smthbig.shadow.setup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smthbig.shadow.databinding.ActivitySetupBinding;
import com.smthbig.shadow.setup.permissions.PermissionActivity;
import com.smthbig.shadow.theme.ThemeManager;

public class SetupActivity extends AppCompatActivity {

    private ActivitySetupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ThemeManager.applyWallpaper(this);

        binding.continueButton.setEnabled(false);

        binding.checkboxAccept.setOnCheckedChangeListener(
                (buttonView, isChecked) -> binding.continueButton.setEnabled(isChecked));

        binding.continueButton.setOnClickListener(v -> {
            SetupManager.markSetupDone(this);

            try {
                startActivity(new Intent(this, PermissionActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error starting permission screen",
                        Toast.LENGTH_LONG).show();
            }

            finish();
        });
    }
}
