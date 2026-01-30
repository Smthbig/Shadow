package com.smthbig.shadow.data.limits;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

public final class AppLimitStore {

    private static final String PREFS = "shadow_app_limits";
    private static final String KEY_PREFIX = "limit_";

    private static final long DEFAULT_LIMIT_MS =
            TimeUnit.MINUTES.toMillis(60);

    private final SharedPreferences prefs;

    public AppLimitStore(Context context) {
        prefs = context
                .getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /* -------- Public API -------- */

    /** Returns per-app limit or default if not set */
    public long getLimitMs(String packageName) {
        return prefs.getLong(
                key(packageName),
                DEFAULT_LIMIT_MS
        );
    }

    /** Sets per-app daily limit */
    public void setLimitMs(String packageName, long limitMs) {
        if (limitMs <= 0) return;

        prefs.edit()
                .putLong(key(packageName), limitMs)
                .apply();
    }

    /** Removes custom limit → fallback to default */
    public void clearLimit(String packageName) {
        prefs.edit()
                .remove(key(packageName))
                .apply();
    }

    /* -------- Internals -------- */

    private String key(String pkg) {
        return KEY_PREFIX + pkg;
    }
}