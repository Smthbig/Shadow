package com.smthbig.shadow.extension;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public final class ExtensionGrantActivity extends Activity {

    private ExtensionEngine extensionEngine;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        extensionEngine = new ExtensionEngine(this);
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

        Button add5 = createButton("Add 5 minutes",
                TimeUnit.MINUTES.toMillis(5));

        Button add10 = createButton("Add 10 minutes",
                TimeUnit.MINUTES.toMillis(10));

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
        if (!extensionEngine.canGrant(ms)) {
            statusText.setText(
                    "Daily extension limit reached"
            );
            return;
        }

        extensionEngine.grant(ms);
        updateStatus();
    }

    private void updateStatus() {
        long remaining =
                extensionEngine.getRemainingMs();

        statusText.setText(
                "Extension remaining:\n"
                        + TimeUnit.MILLISECONDS.toMinutes(remaining)
                        + " minutes"
        );
    }
}