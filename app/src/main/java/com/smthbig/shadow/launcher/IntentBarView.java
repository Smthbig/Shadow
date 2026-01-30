package com.smthbig.shadow.launcher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

public class IntentBarView extends FrameLayout {

    public interface Callback {
        void onIntentEntered(String text);
        void onDismiss();
    }

    private final EditText input;
    private final GradientDrawable background;

    public IntentBarView(Context context, Callback callback) {
        super(context);

        //  Premium glass-like background
        background = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        0xEE111111,
                        0xEE0C0C0C
                }
        );
        background.setCornerRadius(28f);
        background.setStroke(1, 0x22FFFFFF);
        setBackground(background);

        setPadding(32, 28, 32, 28);
        setFocusable(true);
        setFocusableInTouchMode(true);

        //  Input field
        input = new EditText(context);
        input.setHint("> type app");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0xFF666666);
        input.setTextSize(20f);
        input.setLetterSpacing(0.03f);
        input.setBackground(null);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        input.setSingleLine(true);
        input.setPadding(12, 20, 12, 20);

        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.BOTTOM;
        lp.setMargins(56, 48, 56, 112);

        addView(input, lp);

        //  Key handling
        input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                callback.onIntentEntered(
                        input.getText().toString().trim()
                );
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                callback.onDismiss();
                return true;
            }

            return false;
        });

        //  Focus feedback (premium, silent)
        input.setOnFocusChangeListener((v, hasFocus) -> {
            background.setStroke(
                    1,
                    hasFocus ? 0x55FFFFFF : 0x22FFFFFF
            );
            input.setHintTextColor(
                    hasFocus ? 0xFF888888 : 0xFF555555
            );
        });

        input.requestFocus();
    }
}