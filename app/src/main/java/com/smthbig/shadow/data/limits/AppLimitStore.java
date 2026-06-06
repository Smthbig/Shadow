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

    public long getLimitMs(String pkg) {
        if (!prefs.contains(key(pkg))) {
            return 0;
        }
        return prefs.getLong(key(pkg), 0);
    }

    public boolean hasExplicitLimit(String pkg) {
        return prefs.contains(key(pkg));
    }

    public void setLimitMs(String pkg, long limitMs) {
        if (limitMs == UNLIMITED) {
            prefs.edit().putLong(key(pkg), UNLIMITED).apply();
            return;
        }
        if (limitMs <= 0) return;
        prefs.edit().putLong(key(pkg), limitMs).apply();
    }

    public void clearLimit(String pkg) {
        prefs.edit().remove(key(pkg)).apply();
    }

    public boolean isUnlimited(String pkg) {
        if (!hasExplicitLimit(pkg)) return true;
        return getLimitMs(pkg) == UNLIMITED;
    }

    public long getRemainingBaseMs(String pkg, long usedMs) {
        long limit = getLimitMs(pkg);
        if (limit <= 0) return Long.MAX_VALUE;
        if (limit == UNLIMITED) return Long.MAX_VALUE;
        return Math.max(0, limit - usedMs);
    }

    public boolean isLimitReached(String pkg, long usedMs) {
        long limit = getLimitMs(pkg);
        if (limit <= 0) return false;
        if (limit == UNLIMITED) return false;
        return usedMs >= limit;
    }

    public String getReadableLimit(String pkg) {
        if (!hasExplicitLimit(pkg)) return "No limit set";
        long limit = getLimitMs(pkg);
        if (limit == UNLIMITED) return "Unlimited";
        long minutes = limit / (60 * 1000);
        return minutes + " min";
    }

    private String key(String pkg) {
        return KEY_PREFIX + pkg;
    }
}
