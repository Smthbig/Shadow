package com.smthbig.shadow.repository;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class AppRepository {

    private static final String TAG = "AppRepository";
    private static final long CACHE_TTL_MS = TimeUnit.SECONDS.toMillis(60);

    private final PackageManager pm;
    private final Context context;

    private List<ResolveInfo> cachedLauncherApps;
    private List<ApplicationInfo> cachedInstalledApps;
    private long lastLauncherFetch;
    private long lastInstalledFetch;

    public AppRepository(Context context) {
        this.context = context.getApplicationContext();
        this.pm = context.getPackageManager();
    }

    public List<ResolveInfo> getLauncherApps() {
        long now = System.currentTimeMillis();
        if (cachedLauncherApps == null || (now - lastLauncherFetch) > CACHE_TTL_MS) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            cachedLauncherApps = pm.queryIntentActivities(intent, 0);
            lastLauncherFetch = now;
            Log.d(TAG, "Cached " + cachedLauncherApps.size() + " launcher apps");
        }
        return cachedLauncherApps;
    }

    public List<ApplicationInfo> getLaunchableApps() {
        long now = System.currentTimeMillis();
        if (cachedInstalledApps == null || (now - lastInstalledFetch) > CACHE_TTL_MS) {
            List<ApplicationInfo> all = pm.getInstalledApplications(0);
            cachedInstalledApps = new ArrayList<>();
            for (ApplicationInfo app : all) {
                if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                    cachedInstalledApps.add(app);
                }
            }
            cachedInstalledApps.sort(Comparator.comparing(
                    a -> pm.getApplicationLabel(a).toString().toLowerCase()));
            lastInstalledFetch = now;
            Log.d(TAG, "Cached " + cachedInstalledApps.size() + " launchable apps");
        }
        return cachedInstalledApps;
    }

    public void invalidate() {
        cachedLauncherApps = null;
        cachedInstalledApps = null;
    }
}
