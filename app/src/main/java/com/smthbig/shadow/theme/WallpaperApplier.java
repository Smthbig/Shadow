package com.smthbig.shadow.theme;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.smthbig.shadow.R;

public final class WallpaperApplier {

    private WallpaperApplier() {}

    public static void apply(Activity activity) {
        String themeMode = ThemeManager.getTheme(activity);
        String bgType = ThemeManager.getBackground(activity);
        
        View backgroundLayer = activity.findViewById(R.id.background_layer);
        Window window = activity.getWindow();

        // 🚀 System Wallpaper logic (Translucency)
        boolean isSystemWallpaper = "system_wallpaper".equals(bgType);

        if (isSystemWallpaper) {
            if (backgroundLayer != null) {
                backgroundLayer.setBackgroundColor(Color.TRANSPARENT);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
            // Add a subtle tint to ensure contrast
            int glowColor = getThemeColor(activity, R.attr.shadowGlow);
            window.setBackgroundDrawable(new ColorDrawable(glowColor));
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
            applySpecificBackground(activity, backgroundLayer, bgType);
        }
    }

    private static void applySpecificBackground(Activity activity, View layer, String type) {
        int resId;
        switch (type) {
            case "distorted": resId = R.drawable.bg_distorted; break;
            case "mesh": resId = R.drawable.bg_shadow_mesh; break;
            case "aurora": resId = R.drawable.bg_shadow_aurora; break;
            default: resId = R.drawable.bg_shadow_gradient; break;
        }

        if (layer != null) {
            layer.setBackgroundResource(resId);
        } else {
            activity.getWindow().setBackgroundDrawableResource(resId);
        }
    }

    private static int getThemeColor(Activity activity, int attr) {
        TypedValue typedValue = new TypedValue();
        if (activity.getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        }
        return Color.TRANSPARENT;
    }
}
