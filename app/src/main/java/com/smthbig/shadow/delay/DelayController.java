package com.smthbig.shadow.delay;

import android.content.Context;
import android.content.Intent;

public final class DelayController {

    private final Context appContext;

    public DelayController(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /* ---------- DELAY / LAUNCH ---------- */

    public void launchWithDelay(
            Intent target,
            long delayMs,
            String reason,
            boolean usingExtension
    ) {
        if (target == null || target.getComponent() == null) return;

        if (delayMs <= 0) {
            target.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );
            appContext.startActivity(target);
            return;
        }

        appContext.startActivity(
                DelayOverlayActivity.delay(
                        appContext,
                        target,
                        delayMs,
                        reason,
                        usingExtension
                )
        );
    }

    /* ---------- BLOCK ---------- */

    public void block(
            Intent target,
            String reason
    ) {
        if (target == null || target.getComponent() == null) return;

        appContext.startActivity(
                DelayOverlayActivity.block(
                        appContext,
                        target, // 🔥 REQUIRED FIX
                        reason
                )
        );
    }
}