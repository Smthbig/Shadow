package com.smthbig.shadow.system;

import android.app.AppOpsManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public final class UsagePermissionHelper {

    private UsagePermissionHelper() {}

    public static boolean hasUsageAccess(Context context) {
        AppOpsManager appOps =
                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOps == null) return false;

        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName()
        );

        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static boolean isDefaultHomeApp(Context context) {
        PackageManager pm = context.getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager =
                    (RoleManager) context.getSystemService(Context.ROLE_SERVICE);

            if (roleManager == null) return false;

            return roleManager.isRoleHeld(RoleManager.ROLE_HOME);
        }

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);

        String defaultHome =
                pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                        .activityInfo.packageName;

        return context.getPackageName().equals(defaultHome);
    }

    public static boolean hasRequiredPermissions(Context context) {
        return hasUsageAccess(context) && isDefaultHomeApp(context);
    }
}