package com.smthbig.shadow.launcher.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.smthbig.shadow.R;

import java.util.ArrayList;
import java.util.List;

public class IntentBarView extends FrameLayout {

    public interface Callback {
        void onIntentEntered(String text);
        void onDismiss();
        void onSettingsClick();
    }

    private TextInputEditText input;
    private View settingsBtn;
    private RecyclerView suggestionsList;
    private AppSuggestionAdapter adapter;
    private List<ResolveInfo> allApps = new ArrayList<>();
    private List<ResolveInfo> filteredApps = new ArrayList<>();

    public IntentBarView(Context context, Callback callback) {
        super(context);
        init(context, callback);
    }

    private void init(Context context, Callback callback) {
        LayoutInflater.from(context).inflate(R.layout.view_intent_bar, this, true);

        input = findViewById(R.id.input);
        settingsBtn = findViewById(R.id.settings_btn);
        suggestionsList = findViewById(R.id.suggestions_list);

        loadApps();

        if (suggestionsList != null) {
            suggestionsList.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new AppSuggestionAdapter(getContext(), filteredApps, pkg -> {
                hideKeyboard();
                callback.onIntentEntered(pkg);
            });
            suggestionsList.setAdapter(adapter);
            suggestionsList.setVisibility(GONE);
        }

        setupInput(callback);
        setupSettings(callback);
    }

    private void loadApps() {
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = pm.queryIntentActivities(intent, 0);
    }

    private void setupInput(Callback callback) {
        if (input == null) return;

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        input.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = event != null &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    isEnter) {
                submit(callback);
                return true;
            }
            return false;
        });

        input.post(() -> {
            if (!isAttachedToWindow()) return;
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void filterApps(String query) {
        filteredApps.clear();
        if (query.isEmpty()) {
            suggestionsList.setVisibility(GONE);
        } else {
            PackageManager pm = getContext().getPackageManager();
            String lowerQuery = query.toLowerCase();
            for (ResolveInfo app : allApps) {
                String label = app.loadLabel(pm).toString().toLowerCase();
                if (label.contains(lowerQuery)) {
                    filteredApps.add(app);
                }
            }
            if (!filteredApps.isEmpty()) {
                suggestionsList.setVisibility(VISIBLE);
                adapter.notifyDataSetChanged();
            } else {
                suggestionsList.setVisibility(GONE);
            }
        }
    }

    private void submit(Callback callback) {
        if (callback == null) return;
        String text = (input != null && input.getText() != null) ? input.getText().toString().trim() : "";
        hideKeyboard();
        if (!text.isEmpty()) {
            callback.onIntentEntered(text);
        } else {
            callback.onDismiss();
        }
    }

    private void setupSettings(Callback callback) {
        if (settingsBtn == null || callback == null) return;
        settingsBtn.setOnClickListener(v -> callback.onSettingsClick());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && input != null) {
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }
}
