package com.smthbig.shadow.launcher.core;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.policy.TimePolicyEngine;
import com.smthbig.shadow.tracking.UsageTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class LauncherController {

    private static final long DEFAULT_LIMIT_MS = TimeUnit.MINUTES.toMillis(60);
    private static final long LAUNCH_COOLDOWN_MS = 500; // anti-spam

    // Heat settings
    private static final long HEAT_DECAY_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long PENALTY_PER_OPEN = 3000; // 3 seconds penalty per rapid open

    private final Context appContext;
    private final TimePolicyEngine policyEngine;
    private final AppLimitStore appLimitStore;
    private final UsageTracker usageTracker;
    private final ExtensionEngine extensionEngine;
    private final AppLauncher appLauncher;
    private final FeatureStore featureStore;

    private final Map<String, Long> lastLaunchMap = new HashMap<>();
    private final Map<String, Long> heatMap = new HashMap<>();

    public LauncherController(Context context) {
        this.appContext = context.getApplicationContext();
        this.policyEngine = new TimePolicyEngine();
        this.appLimitStore = new AppLimitStore(appContext);
        this.usageTracker = new UsageTracker(appContext);
        this.extensionEngine = new ExtensionEngine(appContext);
        this.appLauncher = new AppLauncher(appContext);
        this.featureStore = new FeatureStore(appContext);
    }

    /* ========================================================= */
    /* ================= ENTRY ================================= */
    /* ========================================================= */

    public void handleIntentText(String query) {

        if (query == null) return;

        // 1. Direct package launch (from suggestions)
        if (isPackageInstalled(query)) {
            handleLaunch(query);
            return;
        }

        // 2. Resolve from label
        String cleaned = normalize(query);
        if (cleaned.isEmpty()) return;

        String pkg = resolvePackage(cleaned);
        if (pkg == null) return;

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

    /* ========================================================= */
    /* ================= RESOLVE =============================== */
    /* ========================================================= */

    private String resolvePackage(String normalizedQuery) {

        PackageManager pm = appContext.getPackageManager();

        Intent base = new Intent(Intent.ACTION_MAIN);
        base.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(base, 0);
        if (apps == null) return null;

        ResolveInfo chosen = null;

        for (ResolveInfo info : apps) {

            String label = normalize(info.loadLabel(pm).toString());

            if (label.equals(normalizedQuery)) {
                return info.activityInfo.packageName;
            }

            if (chosen == null && label.startsWith(normalizedQuery)) {
                chosen = info;
            }
        }

        return chosen != null ? chosen.activityInfo.packageName : null;
    }

    /* ========================================================= */
    /* ================= CORE ================================== */
    /* ========================================================= */

    private void handleLaunch(String pkg) {

        if (pkg == null || pkg.isEmpty()) return;

        // 🚀 FEATURE: WHITELIST BYPASS
        if (featureStore.isWhitelisted(pkg)) {
            launchApp(pkg);
            return;
        }

        long now = System.currentTimeMillis();

        /* ---------- HEAT CALCULATION ---------- */
        long heatPenalty = calculateHeatPenalty(pkg, now);
        
        // 🚀 FEATURE: DEEP FOCUS (Double Heat Penalty)
        if (featureStore.isDeepFocusEnabled()) {
            heatPenalty *= 2;
        }

        /* ---------- ANTI-SPAM (Strict) ---------- */
        Long last = lastLaunchMap.get(pkg);
        if (last != null && (now - last) < LAUNCH_COOLDOWN_MS) {
            return;
        }
        lastLaunchMap.put(pkg, now);

        /* ---------- USAGE & LIMIT ---------- */
        long usedMs = usageTracker.getTodayUsageMs(pkg);
        long totalLimitMs = appLimitStore.getLimitMs(pkg);
        if (totalLimitMs <= 0) totalLimitMs = DEFAULT_LIMIT_MS;

        long remainingBaseMs = appLimitStore.getRemainingBaseMs(pkg, usedMs);
        long remainingExtensionMs = extensionEngine.getRemainingMs(pkg);

        /* ---------- POLICY ---------- */
        TimePolicyEngine.Decision decision =
                policyEngine.evaluate(remainingBaseMs, totalLimitMs, remainingExtensionMs, heatPenalty);

        // 🚀 FEATURE: DEEP FOCUS (Double Delay)
        long finalDelay = decision.delayMs;
        if (featureStore.isDeepFocusEnabled() && finalDelay > 0) {
            finalDelay *= 2;
        }

        /* ---------- ACTION ---------- */

        if (decision.blocked) {
            usageTracker.logBlock();
            appLauncher.launch(DelayOverlayActivity.block(appContext, pkg, decision.reason));
            return;
        }

        if (finalDelay > 0) {
            usageTracker.logDelay();
            // Increase heat on delayed attempts
            updateHeat(pkg, now);

            if (decision.usingExtension) {
                extensionEngine.consume(pkg, finalDelay);
            }

            appLauncher.launch(
                    DelayOverlayActivity.delay(
                            appContext,
                            pkg,
                            finalDelay,
                            decision.reason,
                            decision.usingExtension));
            return;
        }

        /* ---------- SAFE LAUNCH ---------- */
        // If launched frequently even without delay, increase heat
        if (last != null && (now - last) < TimeUnit.MINUTES.toMillis(1)) {
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
        return (long) (PENALTY_PER_OPEN * factor);
    }

    private void updateHeat(String pkg, long now) {
        heatMap.put(pkg, now);
    }

    /* ========================================================= */
    /* ================= SAFE LAUNCH ============================ */
    /* ========================================================= */

    private void launchApp(String pkg) {

        try {
            Intent launchIntent = appContext.getPackageManager().getLaunchIntentForPackage(pkg);

            if (launchIntent != null) {
                appLauncher.launch(launchIntent);
            }

        } catch (Exception ignored) {
        }
    }

    /* ========================================================= */
    /* ================= UTILS ================================= */
    /* ========================================================= */

    private String normalize(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
