package com.smthbig.shadow.theme;

import android.app.Activity;
import android.content.Intent;

public final class ThemeRestarter {

    private ThemeRestarter() {}

    public static void restart(Activity activity) {

        Intent intent =
                activity.getPackageManager()
                        .getLaunchIntentForPackage(activity.getPackageName());

        if (intent == null) {
            // fallback safety (should not happen normally)
            activity.recreate();
            return;
        }

        // Clear full task and restart fresh
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        activity.startActivity(intent);

        // Finish current activity stack safely
        activity.finish();
    }
}