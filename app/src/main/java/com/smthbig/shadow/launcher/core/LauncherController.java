package com.smthbig.shadow.launcher.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.policy.TimePolicyEngine;
import com.smthbig.shadow.tracking.UsageTracker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LauncherController {

    private static final long DEFAULT_LIMIT_MS = TimeUnit.MINUTES.toMillis(60);

    private final Context appContext;
    private final TimePolicyEngine policyEngine;
    private final AppLimitStore appLimitStore;
    private final UsageTracker usageTracker;
    private final ExtensionEngine extensionEngine;

    public LauncherController(Context context) {
        this.appContext = context.getApplicationContext();
        this.policyEngine = new TimePolicyEngine();
        this.appLimitStore = new AppLimitStore(appContext);
        this.usageTracker = new UsageTracker(appContext);
        this.extensionEngine = new ExtensionEngine(appContext);
    }

    /* ========================================================= */
    /* ================= ENTRY ================================= */
    /* ========================================================= */

    public void handleIntentText(String query) {

        if (query == null) return;

        String cleaned = normalize(query);
        if (cleaned.isEmpty()) return;

        String pkg = resolvePackage(cleaned);
        if (pkg == null) return;

        handleLaunch(pkg);
    }

    /* ========================================================= */
    /* ================= RESOLVE =============================== */
    /* ========================================================= */

    private String resolvePackage(String normalizedQuery) {

        PackageManager pm = appContext.getPackageManager();

        Intent base = new Intent(Intent.ACTION_MAIN);
        base.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(base, 0);
        if (apps == null || apps.isEmpty()) return null;

        ResolveInfo chosen = null;

        for (ResolveInfo info : apps) {

            String label = normalize(info.loadLabel(pm).toString());

            if (label.equals(normalizedQuery)) {
                chosen = info;
                break;
            }

            if (chosen == null && label.startsWith(normalizedQuery)) {
                chosen = info;
            }
        }

        if (chosen == null) return null;

        return chosen.activityInfo.packageName; // ✅ ONLY RETURN PKG
    }

    /* ========================================================= */
    /* ================= CORE ================================== */
    /* ========================================================= */

    private void handleLaunch(String pkg) {

        if (pkg == null || pkg.isEmpty()) return;

        /* ---------- USAGE ---------- */

        long usedMs = usageTracker.getTodayUsageMs(pkg);

        /* ---------- BASE LIMIT ---------- */

        long baseLimitMs = appLimitStore.getLimitMs(pkg);

        if (baseLimitMs <= 0) {
            baseLimitMs = DEFAULT_LIMIT_MS;
        }

        /* ---------- DERIVED STATE ---------- */

        long remainingBaseMs = appLimitStore.getRemainingBaseMs(pkg, usedMs);

        long remainingExtensionMs = extensionEngine.getRemainingMs(pkg, usedMs, baseLimitMs);

        /* ---------- POLICY ---------- */

        TimePolicyEngine.Decision decision =
                policyEngine.evaluate(remainingBaseMs, remainingExtensionMs);

        /* ---------- ACTION ---------- */

        if (decision.blocked) {
            appContext.startActivity(DelayOverlayActivity.block(appContext, pkg, decision.reason));
            return;
        }

        if (decision.delayMs > 0) {
            appContext.startActivity(
                    DelayOverlayActivity.delay(
                            appContext,
                            pkg,
                            decision.delayMs,
                            decision.reason,
                            decision.usingExtension));
            return;
        }

        /* ---------- SAFE LAUNCH ---------- */

        launchApp(pkg);
    }

    /* ========================================================= */
    /* ================= SAFE LAUNCH ============================ */
    /* ========================================================= */

    private void launchApp(String pkg) {

        try {
            Intent launchIntent = appContext.getPackageManager().getLaunchIntentForPackage(pkg);

            if (launchIntent == null) return;

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(launchIntent);

        } catch (Exception ignored) {
            // prevent crash
        }
    }

    /* ========================================================= */
    /* ================= UTILS ================================= */
    /* ========================================================= */

    private String normalize(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
