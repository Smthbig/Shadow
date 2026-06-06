package com.smthbig.shadow.launcher.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.Toast;

import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.policy.TimePolicyEngine;
import com.smthbig.shadow.repository.AppRepository;
import com.smthbig.shadow.tracking.FrictionStore;
import com.smthbig.shadow.tracking.UsageTracker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class LauncherController {

    private static final String TAG = "LauncherCtrl";
    private static final long DEFAULT_LIMIT_MS = TimeUnit.MINUTES.toMillis(60);
    private static final long LAUNCH_COOLDOWN_MS = 500;
    private static final long HEAT_DECAY_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long PENALTY_PER_OPEN_MS = 3000;
    private static final long RAPID_LAUNCH_WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    private final Context appContext;
    private final TimePolicyEngine policyEngine;
    private final AppLimitStore appLimitStore;
    private final UsageTracker usageTracker;
    private final ExtensionEngine extensionEngine;
    private final AppLauncher appLauncher;
    private final FeatureStore featureStore;
    private final AppRepository appRepository;

    private final Map<String, Long> lastLaunchMap;
    private final Map<String, Long> heatMap;

    public LauncherController(Context context) {
        ServiceLocator locator = ServiceLocator.getInstance();
        this.appContext = context.getApplicationContext();
        this.policyEngine = locator.getTimePolicyEngine();
        this.appLimitStore = locator.getAppLimitStore();
        this.usageTracker = locator.getUsageTracker();
        this.extensionEngine = locator.getExtensionEngine();
        this.appLauncher = locator.getAppLauncher();
        this.featureStore = locator.getFeatureStore();
        this.appRepository = locator.getAppRepository();

        FrictionStore frictionStore = FrictionStore.getInstance();
        this.lastLaunchMap = frictionStore.getLastLaunchMap();
        this.heatMap = frictionStore.getHeatMap();
    }

    public void handleIntentText(String query) {
        if (query == null || query.trim().isEmpty()) return;

        String trimmed = query.trim();

        if (isPackageInstalled(trimmed)) {
            Log.d(TAG, "Direct package launch: " + trimmed);
            handleLaunch(trimmed);
            return;
        }

        String cleaned = normalize(trimmed);
        if (cleaned.isEmpty()) return;

        String pkg = resolvePackage(cleaned);
        if (pkg == null) {
            Log.w(TAG, "No package found for query: " + trimmed);
            Toast.makeText(appContext, "No app found: " + trimmed, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Resolved: " + pkg + " from: " + trimmed);
        handleLaunch(pkg);
    }

    private boolean isPackageInstalled(String pkg) {
        try {
            appContext.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String resolvePackage(String normalizedQuery) {
        PackageManager pm = appContext.getPackageManager();
        List<ResolveInfo> apps = appRepository.getLauncherApps();
        if (apps == null || apps.isEmpty()) return null;

        ResolveInfo chosen = null;

        for (ResolveInfo info : apps) {
            if (info == null || info.activityInfo == null) continue;

            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null ? normalize(labelSeq.toString()) : "";

            if (label.equals(normalizedQuery)) {
                return info.activityInfo.packageName;
            }

            if (chosen == null && label.startsWith(normalizedQuery)) {
                chosen = info;
            }
        }

        return chosen != null && chosen.activityInfo != null
                ? chosen.activityInfo.packageName
                : null;
    }

    private void handleLaunch(String pkg) {
        if (pkg == null || pkg.isEmpty()) return;

        if (featureStore.isWhitelisted(pkg)) {
            Log.d(TAG, "Whitelisted, direct launch: " + pkg);
            launchApp(pkg);
            return;
        }

        long now = System.currentTimeMillis();
        long heatPenalty = calculateHeatPenalty(pkg, now);

        if (featureStore.isDeepFocusEnabled()) {
            heatPenalty *= 2;
        }

        Long last = lastLaunchMap.get(pkg);
        if (last != null && (now - last) < LAUNCH_COOLDOWN_MS) {
            Log.d(TAG, "Cooldown active for: " + pkg);
            return;
        }
        lastLaunchMap.put(pkg, now);

        long usedMs = usageTracker.getTodayUsageMs(pkg);
        long totalLimitMs = appLimitStore.getLimitMs(pkg);
        if (totalLimitMs <= 0) totalLimitMs = DEFAULT_LIMIT_MS;

        long remainingBaseMs = appLimitStore.getRemainingBaseMs(pkg, usedMs);
        long remainingExtensionMs = extensionEngine.getRemainingMs(pkg);

        TimePolicyEngine.Decision decision =
                policyEngine.evaluate(remainingBaseMs, totalLimitMs, remainingExtensionMs, heatPenalty);

        long finalDelay = decision.delayMs;
        if (featureStore.isDeepFocusEnabled() && finalDelay > 0) {
            finalDelay *= 2;
        }

        Log.d(TAG, "Decision for " + pkg + ": blocked=" + decision.blocked
                + " delay=" + finalDelay + "ms reason=" + decision.reason);

        if (decision.blocked) {
            usageTracker.logBlock();
            appLauncher.launch(DelayOverlayActivity.block(appContext, pkg, decision.reason));
            return;
        }

        if (finalDelay > 0) {
            usageTracker.logDelay();
            updateHeat(pkg, now);

            if (decision.usingExtension) {
                extensionEngine.consume(pkg, finalDelay);
            }

            appLauncher.launch(DelayOverlayActivity.delay(
                    appContext, pkg, finalDelay, decision.reason, decision.usingExtension));
            return;
        }

        if (last != null && (now - last) < RAPID_LAUNCH_WINDOW_MS) {
            updateHeat(pkg, now);
        }

        launchApp(pkg);
    }

    private long calculateHeatPenalty(String pkg, long now) {
        Long lastHeatUpdate = heatMap.get(pkg);
        if (lastHeatUpdate == null) return 0;

        long elapsed = now - lastHeatUpdate;
        if (elapsed >= HEAT_DECAY_MS) {
            heatMap.remove(pkg);
            return 0;
        }

        float factor = 1.0f - ((float) elapsed / HEAT_DECAY_MS);
        return (long) (PENALTY_PER_OPEN_MS * factor);
    }

    private void updateHeat(String pkg, long now) {
        heatMap.put(pkg, now);
    }

    private void launchApp(String pkg) {
        try {
            Intent launchIntent = appContext.getPackageManager().getLaunchIntentForPackage(pkg);
            if (launchIntent != null) {
                appLauncher.launch(launchIntent);
            } else {
                Log.w(TAG, "No launch intent for: " + pkg);
                Toast.makeText(appContext, "Cannot launch app", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch: " + pkg, e);
            Toast.makeText(appContext, "Failed to launch app", Toast.LENGTH_SHORT).show();
        }
    }

    private static String normalize(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
