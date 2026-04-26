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

        // 🚀 ONLY IN TRANSPARENT THEME
        boolean isTransparentTheme = 
                ThemeMode.TRANSPARENT_LIGHT.equals(themeMode) || 
                ThemeMode.TRANSPARENT_DARK.equals(themeMode);

        if (isTransparentTheme) {
            if (backgroundLayer != null) {
                backgroundLayer.setBackgroundColor(Color.TRANSPARENT);
            }
            
            int glowColor = getThemeColor(activity, R.attr.shadowGlow);
            window.setBackgroundDrawable(new ColorDrawable(glowColor));
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        } else {
            // Standard behavior for ALL other themes (including Glass)
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
