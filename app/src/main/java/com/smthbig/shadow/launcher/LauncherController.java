package com.smthbig.shadow.launcher;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.policy.TimePolicyEngine;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LauncherController {

    private final Context appContext;
    private final TimePolicyEngine policyEngine;
    private final ExtensionEngine extensionEngine;
    private final AppLimitStore appLimitStore;

    public LauncherController(Context context) {
        this.appContext = context.getApplicationContext();
        this.policyEngine = new TimePolicyEngine();
        this.extensionEngine = new ExtensionEngine(appContext);
        this.appLimitStore = new AppLimitStore(appContext);
    }

    /* Entry point from UI */
    public void handleIntentText(String query) {
        if (query == null) return;

        String cleaned = normalize(query);
        if (cleaned.isEmpty()) return;

        Intent resolved = resolveApp(cleaned);
        if (resolved == null) return;

        handleLaunch(resolved);
    }

    /* Resolve app by label */
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

            if (chosen == null && label.startsWith(normalizedQuery)) {
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

    /* Core policy decision */
    private void handleLaunch(Intent originalIntent) {
        if (originalIntent.getComponent() == null) return;

        Intent target = new Intent(originalIntent);
        String pkg = target.getComponent().getPackageName();

        // 🔹 PER-APP BASE LIMIT
        long baseLimitMs =
                appLimitStore.getLimitMs(pkg);

        long usedBaseMs =
                getTodayForegroundTime(pkg);

        long remainingBaseMs =
                Math.max(0, baseLimitMs - usedBaseMs);

        long remainingExtensionMs =
                extensionEngine.getRemainingMs();

        TimePolicyEngine.Decision decision =
                policyEngine.evaluate(
                        remainingBaseMs,
                        remainingExtensionMs
                );

        if (decision.blocked) {
            appContext.startActivity(
                    DelayOverlayActivity.block(
                            appContext,
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

    /* Accurate foreground time since midnight */
    private long getTodayForegroundTime(String pkg) {
        UsageStatsManager usm =
                (UsageStatsManager)
                        appContext.getSystemService(
                                Context.USAGE_STATS_SERVICE
                        );

        if (usm == null) return 0L;

        long start = startOfToday();
        long now = System.currentTimeMillis();

        UsageEvents events =
                usm.queryEvents(start, now);

        UsageEvents.Event event =
                new UsageEvents.Event();

        long total = 0L;
        Long lastResume = null;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            if (!pkg.equals(event.getPackageName())) continue;

            if (event.getEventType()
                    == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastResume = event.getTimeStamp();
            }

            if (event.getEventType()
                    == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (lastResume != null) {
                    total +=
                            (event.getTimeStamp() - lastResume);
                    lastResume = null;
                }
            }
        }

        if (lastResume != null) {
            total += (now - lastResume);
        }

        return Math.max(0L, total);
    }

    private long startOfToday() {
        long now = System.currentTimeMillis();
        return now - (now % TimeUnit.DAYS.toMillis(1));
    }

    private String normalize(String text) {
        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}