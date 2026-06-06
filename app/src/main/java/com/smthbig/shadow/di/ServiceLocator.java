package com.smthbig.shadow.di;

import android.content.Context;

import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.launcher.core.AppLauncher;
import com.smthbig.shadow.launcher.core.LauncherController;
import com.smthbig.shadow.policy.TimePolicyEngine;
import com.smthbig.shadow.repository.AppRepository;
import com.smthbig.shadow.repository.UsageStatsRepository;
import com.smthbig.shadow.theme.ThemeStore;
import com.smthbig.shadow.tracking.UsageTracker;

public final class ServiceLocator {

    private static ServiceLocator instance;

    private final Context appContext;
    private UsageStatsRepository usageStatsRepository;
    private AppRepository appRepository;
    private AppLimitStore appLimitStore;
    private ExtensionEngine extensionEngine;
    private FeatureStore featureStore;
    private UsageTracker usageTracker;
    private LauncherController launcherController;
    private TimePolicyEngine timePolicyEngine;
    private AppLauncher appLauncher;

    private ServiceLocator(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized ServiceLocator init(Context context) {
        if (instance == null) {
            instance = new ServiceLocator(context);
        }
        return instance;
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceLocator not initialized. Call init() first.");
        }
        return instance;
    }

    public UsageStatsRepository getUsageStatsRepository() {
        if (usageStatsRepository == null) {
            usageStatsRepository = new UsageStatsRepository(appContext);
        }
        return usageStatsRepository;
    }

    public AppRepository getAppRepository() {
        if (appRepository == null) {
            appRepository = new AppRepository(appContext);
        }
        return appRepository;
    }

    public AppLimitStore getAppLimitStore() {
        if (appLimitStore == null) {
            appLimitStore = new AppLimitStore(appContext);
        }
        return appLimitStore;
    }

    public ExtensionEngine getExtensionEngine() {
        if (extensionEngine == null) {
            extensionEngine = new ExtensionEngine(appContext);
        }
        return extensionEngine;
    }

    public FeatureStore getFeatureStore() {
        if (featureStore == null) {
            featureStore = new FeatureStore(appContext);
        }
        return featureStore;
    }

    public UsageTracker getUsageTracker() {
        if (usageTracker == null) {
            usageTracker = new UsageTracker(appContext, getUsageStatsRepository());
        }
        return usageTracker;
    }

    public TimePolicyEngine getTimePolicyEngine() {
        if (timePolicyEngine == null) {
            timePolicyEngine = new TimePolicyEngine();
        }
        return timePolicyEngine;
    }

    public AppLauncher getAppLauncher() {
        if (appLauncher == null) {
            appLauncher = new AppLauncher(appContext);
        }
        return appLauncher;
    }

    public LauncherController getLauncherController() {
        if (launcherController == null) {
            launcherController = new LauncherController(appContext);
        }
        return launcherController;
    }
}
