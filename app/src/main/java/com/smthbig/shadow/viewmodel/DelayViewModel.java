package com.smthbig.shadow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.extension.ExtensionEngine;

import java.util.concurrent.TimeUnit;

public final class DelayViewModel extends AndroidViewModel {

    private static final long EXTENSION_MS = TimeUnit.MINUTES.toMillis(5);

    private final ExtensionEngine extensionEngine;
    private final MutableLiveData<Long> remainingMs = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> extensionGranted = new MutableLiveData<>(false);
    private final MutableLiveData<String> quote = new MutableLiveData<>("");

    private String pkg;
    private long totalMs;

    public DelayViewModel(@NonNull Application application) {
        super(application);
        this.extensionEngine = ServiceLocator.getInstance().getExtensionEngine();
        this.quote.setValue(getRandomQuote());
    }

    public void initialize(String pkg, long delayMs, long additionalMs) {
        this.pkg = pkg;
        this.totalMs = delayMs + additionalMs;
        this.remainingMs.setValue(totalMs);
    }

    public void tick(long ms) {
        remainingMs.setValue(ms);
    }

    public LiveData<Long> getRemainingMs() {
        return remainingMs;
    }

    public LiveData<Boolean> getExtensionGranted() {
        return extensionGranted;
    }

    public LiveData<String> getQuote() {
        return quote;
    }

    public boolean grantExtension() {
        if (pkg == null) return false;

        boolean granted = extensionEngine.grant(pkg, EXTENSION_MS);
        if (granted) {
            totalMs += EXTENSION_MS;
            remainingMs.setValue(totalMs);
            extensionGranted.setValue(true);
        }
        return granted;
    }

    public long getTotalMs() {
        return totalMs;
    }

    private static String getRandomQuote() {
        String[] quotes = {
            "Is this necessary?",
            "Focus is a choice.",
            "Stay intentional.",
            "One breath of clarity.",
            "Mind over impulse.",
            "Respond, don't react.",
            "Silence the noise.",
            "The best way out is through.",
            "You are the master of your time.",
            "Inhale purpose, exhale distraction."
        };
        return quotes[(int) (Math.random() * quotes.length)];
    }
}
