package com.smthbig.shadow.data.limits;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

public final class AppLimitStore {

    private static final String PREFS = "shadow_app_limits";
    private static final String KEY_PREFIX = "limit_";

    public static final long DEFAULT_LIMIT_MS = TimeUnit.MINUTES.toMillis(60);

    public static final long UNLIMITED = -1L;

    private final SharedPreferences prefs;

    public AppLimitStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /* ========================================================= */
    /* ===================== GET ================================ */
    /* ========================================================= */

    public long getLimitMs(String pkg) {

        if (!prefs.contains(key(pkg))) {
            return DEFAULT_LIMIT_MS;
        }

        return prefs.getLong(key(pkg), DEFAULT_LIMIT_MS);
    }

    /* ========================================================= */
    /* ===================== SET ================================ */
    /* ========================================================= */

    public void setLimitMs(String pkg, long limitMs) {

        // allow unlimited
        if (limitMs == UNLIMITED) {
            prefs.edit().putLong(key(pkg), UNLIMITED).apply();
            return;
        }

        // ignore invalid
        if (limitMs <= 0) return;

        prefs.edit().putLong(key(pkg), limitMs).apply();
    }

    /* ========================================================= */
    /* ===================== CLEAR ============================== */
    /* ========================================================= */

    public void clearLimit(String pkg) {
        prefs.edit().remove(key(pkg)).apply();
    }

    /* ========================================================= */
    /* ===================== HELPERS ============================ */
    /* ========================================================= */

    public boolean isUnlimited(String pkg) {
        return getLimitMs(pkg) == UNLIMITED;
    }

    public long getRemainingBaseMs(String pkg, long usedMs) {

        long limit = getLimitMs(pkg);

        if (limit == UNLIMITED) {
            return Long.MAX_VALUE;
        }

        return Math.max(0, limit - usedMs);
    }

    public boolean isLimitReached(String pkg, long usedMs) {

        long limit = getLimitMs(pkg);

        if (limit == UNLIMITED) return false;

        return usedMs >= limit;
    }

    /* ========================================================= */
    /* ===================== DEBUG ============================== */
    /* ========================================================= */

    public String getReadableLimit(String pkg) {

        long limit = getLimitMs(pkg);

        if (limit == UNLIMITED) {
            return "Unlimited";
        }

        long minutes = limit / (60 * 1000);

        return minutes + " min";
    }

    /* ========================================================= */
    /* ===================== INTERNAL =========================== */
    /* ========================================================= */

    private String key(String pkg) {
        return KEY_PREFIX + pkg;
    }
}
