package com.smthbig.shadow.tracking;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.delay.DelayOverlayActivity;
import com.smthbig.shadow.extension.ExtensionEngine;
import com.smthbig.shadow.launcher.core.AppLauncher;

import java.util.concurrent.TimeUnit;

/**
 * Background service that enforces limits while an app is running.
 * If the limit + extension is consumed, it pulls the user out of the app.
 */
public final class UsageMonitorService extends Service {

    private static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(5);

    private Handler handler;
    private Runnable monitorTask;

    private UsageTracker usageTracker;
    private AppLimitStore limitStore;
    private ExtensionEngine extensionEngine;
    private AppLauncher appLauncher;

    @Override
    public void onCreate() {
        super.onCreate();
        
        usageTracker = new UsageTracker(this);
        limitStore = new AppLimitStore(this);
        extensionEngine = new ExtensionEngine(this);
        appLauncher = new AppLauncher(this);

        handler = new Handler(Looper.getMainLooper());
        monitorTask = this::checkForegroundApp;
        
        startMonitoring();
    }

    private void startMonitoring() {
        handler.postDelayed(monitorTask, POLL_INTERVAL);
    }

    private void checkForegroundApp() {
        
        UsageStatsReader reader = new UsageStatsReader(this);
        String pkg = getForegroundPackage(reader);

        if (pkg != null && !pkg.equals(getPackageName())) {
            
            long usedMs = usageTracker.getTodayUsageMs(pkg);
            long limitMs = limitStore.getLimitMs(pkg);
            long remainingExtensionMs = extensionEngine.getRemainingMs(pkg);

            // If it's a limited app AND it's out of both base and extension time
            if (limitMs > 0 && limitMs != AppLimitStore.UNLIMITED) {
                
                if (usedMs >= (limitMs + remainingExtensionMs)) {
                    // ENFORCE: Pull user out of the app
                    appLauncher.launch(DelayOverlayActivity.block(this, pkg, "Time's up for today"));
                }
            }
        }

        handler.postDelayed(monitorTask, POLL_INTERVAL);
    }

    private String getForegroundPackage(UsageStatsReader reader) {
        return reader.getForegroundPackage();
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
        handler.removeCallbacks(monitorTask);
    }
}