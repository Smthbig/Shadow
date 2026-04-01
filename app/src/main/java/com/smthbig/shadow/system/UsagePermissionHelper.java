package com.smthbig.shadow.system;

import android.app.AppOpsManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

public final class UsagePermissionHelper {

    private UsagePermissionHelper() {}

    /* ---------- USAGE ACCESS ---------- */

    public static boolean hasUsageAccess(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOps == null) return false;

        int mode =
                appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static Intent getUsageAccessIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    /* ---------- DEFAULT HOME ---------- */

    public static boolean isDefaultHomeApp(Context context) {
        PackageManager pm = context.getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);

            if (roleManager == null) return false;

            return roleManager.isRoleHeld(RoleManager.ROLE_HOME);
        }

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);

        if (pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return false;
        }

        String defaultHome =
                pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                        .activityInfo
                        .packageName;

        return context.getPackageName().equals(defaultHome);
    }

    public static Intent getHomeRoleRequestIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);

            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {

                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
            }
        }

        // fallback (shows launcher chooser)
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /* ---------- RESTRICTED SETTINGS (ANDROID 12+) ---------- */

    public static Intent getAppDetailsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    /* ---------- COMBINED ---------- */

    public static boolean hasRequiredPermissions(Context context) {
        return hasUsageAccess(context) && isDefaultHomeApp(context);
    }
}
