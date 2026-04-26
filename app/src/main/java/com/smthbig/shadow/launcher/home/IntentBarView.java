package com.smthbig.shadow.launcher.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.smthbig.shadow.R;

import java.util.ArrayList;
import java.util.List;

public class IntentBarView extends MaterialCardView {

    public interface Callback {
        void onIntentEntered(String text);
        void onDismiss();
    }

    private TextInputEditText input;
    private RecyclerView suggestionsList;
    private AppSuggestionAdapter adapter;
    private List<ResolveInfo> allApps = new ArrayList<>();
    private List<ResolveInfo> filteredApps = new ArrayList<>();
    private Callback callback;

    public IntentBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void init(Context context) {
        // Enforce Card Styling
        setRadius(dpToPx(24));
        setCardElevation(dpToPx(12));
        
        // Resolve outline color from theme
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)) {
            setStrokeColor(typedValue.data);
        }
        
        setStrokeWidth(1);
        
        // Resolve shadowGlass color from theme
        if (getContext().getTheme().resolveAttribute(R.attr.shadowGlass, typedValue, true)) {
            setCardBackgroundColor(typedValue.data);
        } else {
            setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        LayoutInflater.from(context).inflate(R.layout.view_intent_bar, this, true);

        input = findViewById(R.id.input);
        suggestionsList = findViewById(R.id.suggestions_list);

        loadApps();

        if (suggestionsList != null) {
            suggestionsList.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            adapter = new AppSuggestionAdapter(getContext(), filteredApps, pkg -> {
                hideKeyboard();
                if (callback != null) callback.onIntentEntered(pkg);
            });
            suggestionsList.setAdapter(adapter);
            suggestionsList.setVisibility(GONE);
        }

        setupInput();
    }

    private void setupInput() {
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
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
    }

    public void focus() {
        if (input != null) {
            input.setText("");
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void clearFocusAndHide() {
        hideKeyboard();
        if (input != null) input.clearFocus();
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

    private void loadApps() {
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = pm.queryIntentActivities(intent, 0);
    }

    private void submit() {
        String text = (input != null && input.getText() != null) ? input.getText().toString().trim() : "";
        if (!text.isEmpty() && callback != null) {
            callback.onIntentEntered(text);
        } else if (callback != null) {
            callback.onDismiss();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && input != null) imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
