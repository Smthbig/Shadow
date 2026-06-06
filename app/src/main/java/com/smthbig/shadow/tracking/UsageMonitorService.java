package com.smthbig.shadow.tracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.launcher.core.AppLauncher;
import com.smthbig.shadow.repository.UsageStatsRepository;

import java.util.concurrent.TimeUnit;

public final class UsageMonitorService extends Service {

    private static final String TAG = "UsageMonitorSvc";
    private static final long POLL_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);

    private Handler handler;
    private Runnable monitorTask;

    private UsageStatsRepository usageStatsRepository;
    private AppLimitStore limitStore;
    private ExtensionEngine extensionEngine;
    private AppLauncher appLauncher;

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator locator = ServiceLocator.getInstance();
        usageStatsRepository = locator.getUsageStatsRepository();
        limitStore = locator.getAppLimitStore();
        extensionEngine = locator.getExtensionEngine();
        appLauncher = locator.getAppLauncher();

        handler = new Handler(Looper.getMainLooper());
        monitorTask = () -> {
            try {
                checkForegroundApp();
            } catch (Exception e) {
                Log.e(TAG, "Monitor loop exception", e);
            } finally {
                handler.postDelayed(monitorTask, POLL_INTERVAL_MS);
            }
        };

        handler.post(monitorTask);
    }

    private void checkForegroundApp() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isInteractive()) return;

        String pkg = usageStatsRepository.getForegroundPackage();
        if (pkg == null || pkg.equals(getPackageName())) return;

        if (ServiceLocator.getInstance().getFeatureStore().isWhitelisted(pkg)) return;

        long usedMs = usageStatsRepository.getTodayUsageMs(pkg);
        long limitMs = limitStore.getLimitMs(pkg);
        long remainingExtensionMs = extensionEngine.getRemainingMs(pkg);

        if (limitMs <= 0 || limitMs == AppLimitStore.UNLIMITED) return;

        if (usedMs > (limitMs + remainingExtensionMs)) {
            String currentActive = FrictionStore.getInstance().getActiveDelay();
            if (pkg.equals(currentActive)) return;

            FrictionStore.getInstance().setActiveDelay(pkg);
            Log.w(TAG, "Enforcing limit for " + pkg + " used=" + usedMs + " limit=" + limitMs);

            appLauncher.launch(DelayOverlayActivity.delay(
                    this, pkg, 10000,
                    "Time's up. Add extension to continue.", false));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && monitorTask != null) {
            handler.removeCallbacks(monitorTask);
        }
    }
}
