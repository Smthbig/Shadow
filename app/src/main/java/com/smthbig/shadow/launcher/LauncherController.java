package com.smthbig.shadow.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.policy.TimePolicyEngine;
import com.smthbig.shadow.tracking.UsageTracker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LauncherController {

    private static final long DEFAULT_LIMIT_MS =
            TimeUnit.MINUTES.toMillis(60);

    private final Context appContext;
    private final TimePolicyEngine policyEngine;
    private final AppLimitStore appLimitStore;
    private final UsageTracker usageTracker;

    public LauncherController(Context context) {
        this.appContext = context.getApplicationContext();
        this.policyEngine = new TimePolicyEngine();
        this.appLimitStore = new AppLimitStore(appContext);
        this.usageTracker = new UsageTracker(appContext);
    }

    /* ---------- Entry ---------- */

    public void handleIntentText(String query) {
        if (query == null) return;

        String cleaned = normalize(query);
        if (cleaned.isEmpty()) return;

        Intent resolved = resolveApp(cleaned);
        if (resolved == null) return;

        handleLaunch(resolved);
    }

    /* ---------- Resolve App ---------- */

    private Intent resolveApp(String normalizedQuery) {
        PackageManager pm = appContext.getPackageManager();

        Intent base = new Intent(Intent.ACTION_MAIN);
        base.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps =
                pm.queryIntentActivities(base, 0);

        if (apps == null || apps.isEmpty()) return null;

        ResolveInfo chosen = null;

        for (ResolveInfo info : apps) {
            String label =
                    normalize(info.loadLabel(pm).toString());

            if (label.equals(normalizedQuery)) {
                chosen = info;
                break;
            }

            if (chosen == null &&
                    label.startsWith(normalizedQuery)) {
                chosen = info;
            }
        }

        if (chosen == null) return null;

        Intent intent =
                pm.getLaunchIntentForPackage(
                        chosen.activityInfo.packageName
                );

        if (intent != null) {
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );
        }

        return intent;
    }

    /* ---------- Core Logic ---------- */

    private void handleLaunch(Intent originalIntent) {
        if (originalIntent.getComponent() == null) return;

        Intent target = new Intent(originalIntent);
        String pkg = target.getComponent().getPackageName();

        /* ---------- USAGE (also consumes extension internally) ---------- */

        long usedMs =
                usageTracker.getTodayUsageMs(pkg);

        long remainingExtensionMs =
                usageTracker.getRemainingExtensionMs(pkg);

        /* ---------- BASE LIMIT ---------- */

        long baseLimitMs =
                appLimitStore.getLimitMs(pkg);

        if (baseLimitMs <= 0) {
            baseLimitMs = DEFAULT_LIMIT_MS;
        }

        long remainingBaseMs =
                Math.max(0, baseLimitMs - usedMs);

        /* ---------- POLICY ---------- */

        TimePolicyEngine.Decision decision =
                policyEngine.evaluate(
                        remainingBaseMs,
                        remainingExtensionMs
                );

        /* ---------- ACTION ---------- */

        if (decision.blocked) {
            appContext.startActivity(
                    DelayOverlayActivity.block(
                            appContext,
                            target,
                            decision.reason
                    )
            );
            return;
        }

        if (decision.delayMs > 0) {
            appContext.startActivity(
                    DelayOverlayActivity.delay(
                            appContext,
                            target,
                            decision.delayMs,
                            decision.reason,
                            decision.usingExtension
                    )
            );
            return;
        }

        appContext.startActivity(target);
    }

    /* ---------- Utils ---------- */

    private String normalize(String text) {
        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}