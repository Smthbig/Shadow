package com.smthbig.shadow.delay;

import android.content.Context;
import android.content.Intent;

public final class DelayController {

    private final Context appContext;

    public DelayController(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void launchWithDelay(
            Intent target,
            long delayMs,
            String reason,
            boolean usingExtension
    ) {
        if (target == null) return;

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

    public void block(String reason) {
        appContext.startActivity(
                DelayOverlayActivity.block(
                        appContext,
                        reason
                )
        );
    }
}