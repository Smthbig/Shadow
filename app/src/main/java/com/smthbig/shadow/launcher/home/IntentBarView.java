package com.smthbig.shadow.launcher.home;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.smthbig.shadow.R;

public class IntentBarView extends FrameLayout {

    public interface Callback {
        void onIntentEntered(String text);
        void onDismiss();
        void onSettingsClick(); // 🔥 added
    }

    private TextInputEditText input;
    private MaterialButton settingsBtn;

    public IntentBarView(Context context, Callback callback) {
        super(context);
        init(context, callback);
    }

    private void init(Context context, Callback callback) {
        LayoutInflater.from(context)
                .inflate(R.layout.view_intent_bar, this, true);

        input = findViewById(R.id.input);
        settingsBtn = findViewById(R.id.settings_btn);

        setupInput(callback);
        setupSettings(callback);
    }

    /* ---------- INPUT ---------- */

    private void setupInput(Callback callback) {

        if (input == null) return;

        input.setOnEditorActionListener((v, actionId, event) -> {

            boolean isEnter =
                    (event != null &&
                     event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                     event.getAction() == KeyEvent.ACTION_DOWN);

            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_GO ||
                isEnter) {

                submit(callback);
                return true;
            }

            return false;
        });

        // focus + keyboard
        input.post(() -> {
            if (!input.isAttachedToWindow()) return;

            input.requestFocus();

            InputMethodManager imm =
                    (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void submit(Callback callback) {
        if (callback == null) return;

        String text = "";

        if (input != null && input.getText() != null) {
            text = input.getText().toString().trim();
        }

        if (!text.isEmpty()) {
            callback.onIntentEntered(text);
        } else {
            callback.onDismiss();
        }
    }

    /* ---------- SETTINGS ---------- */

    private void setupSettings(Callback callback) {

        if (settingsBtn == null || callback == null) return;

        settingsBtn.setOnClickListener(v -> {
            try {
                callback.onSettingsClick();
            } catch (Exception ignored) {}
        });
    }
}