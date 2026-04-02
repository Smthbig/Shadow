package com.smthbig.shadow.extension;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.smthbig.shadow.theme.ThemeManager;

import java.util.concurrent.TimeUnit;

public final class ExtensionGrantActivity extends Activity {

    private ExtensionEngine extensionEngine;
    private TextView statusText;

    private String pkg; // target app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);

        extensionEngine = new ExtensionEngine(this);

        pkg = getIntent().getStringExtra("pkg");

        if (pkg == null) {
            finish();
            return;
        }

        setContentView(createContent());
        updateStatus();
    }

    private View createContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(48, 48, 48, 48);

        statusText = new TextView(this);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(16f);
        statusText.setGravity(Gravity.CENTER);

        Button add5 = createButton(
                "Add 5 minutes",
                TimeUnit.MINUTES.toMillis(5)
        );

        Button add10 = createButton(
                "Add 10 minutes",
                TimeUnit.MINUTES.toMillis(10)
        );

        root.addView(statusText);
        root.addView(add5);
        root.addView(add10);

        return root;
    }

    private Button createButton(String label, long ms) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(v -> grant(ms));
        return b;
    }

    private void grant(long ms) {

        if (!extensionEngine.canGrant(pkg, ms)) {
            statusText.setText("Daily extension limit reached");
            return;
        }

        boolean success = extensionEngine.grant(pkg, ms);

        if (!success) {
            statusText.setText("Failed to grant extension");
            return;
        }

        //  Relaunch app immediately
        try {
            Intent launch =
                    getPackageManager()
                            .getLaunchIntentForPackage(pkg);

            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            }
        } catch (Exception ignored) {}

        finish();
    }

    private void updateStatus() {
        long remaining =
                extensionEngine.getRemainingMs(pkg);

        statusText.setText(
                "Extension remaining:\n"
                        + TimeUnit.MILLISECONDS.toMinutes(remaining)
                        + " minutes"
        );
    }
}