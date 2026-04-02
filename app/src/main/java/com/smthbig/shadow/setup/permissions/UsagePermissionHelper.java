package com.smthbig.shadow.setup.permissions;

import android.app.AppOpsManager;
import android.app.role.RoleManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.List;

public final class UsagePermissionHelper {

    private UsagePermissionHelper() {}

    /* ========================================================= */
    /* ================= USAGE ACCESS =========================== */
    /* ========================================================= */

    public static boolean hasUsageAccess(Context context) {

        // Step 1: AppOps check (fast path)
        AppOpsManager appOps =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOps == null) return false;

        int mode =
                appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.getPackageName()
                );

        if (mode != AppOpsManager.MODE_ALLOWED) {
            return false;
        }

        // Step 2: Real validation (critical)
        return hasUsageStatsData(context);
    }

    private static boolean hasUsageStatsData(Context context) {
        try {
            UsageStatsManager usm =
                    (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            if (usm == null) return false;

            long now = System.currentTimeMillis();
            long start = now - 60 * 60 * 1000; // last 1 hour

            List<UsageStats> stats =
                    usm.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            start,
                            now
                    );

            return stats != null && !stats.isEmpty();

        } catch (Exception e) {
            return false;
        }
    }

    public static Intent getUsageAccessIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    /* ========================================================= */
    /* ================= DEFAULT HOME =========================== */
    /* ========================================================= */

    public static boolean isDefaultHomeApp(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            RoleManager roleManager =
                    (RoleManager) context.getSystemService(Context.ROLE_SERVICE);

            return roleManager != null
                    && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)
                    && roleManager.isRoleHeld(RoleManager.ROLE_HOME);
        }

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        if (pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return false;
        }

        String packageName =
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        .activityInfo.packageName;

        return context.getPackageName().equals(packageName);
    }

    public static Intent getHomeRoleRequestIntent(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            RoleManager roleManager =
                    (RoleManager) context.getSystemService(Context.ROLE_SERVICE);

            if (roleManager != null
                    && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)
                    && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {

                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
            }
        }

        // fallback
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    /* ========================================================= */
    /* ================= RESTRICTED SETTINGS ==================== */
    /* ========================================================= */

    public static Intent getAppDetailsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(
                android.net.Uri.fromParts("package", context.getPackageName(), null)
        );
        return intent;
    }

    /* ========================================================= */
    /* ================= COMBINED =============================== */
    /* ========================================================= */

    public static boolean hasRequiredPermissions(Context context) {
        return hasUsageAccess(context) && isDefaultHomeApp(context);
    }
}