package com.smthbig.shadow.theme;

import androidx.appcompat.app.AppCompatDelegate;
import com.smthbig.shadow.R;

final class ThemeConfig {

    private ThemeConfig() {}

    static int getNightMode(String mode) {
        switch (mode) {
            case ThemeMode.LIGHT:
            case ThemeMode.GLASS_LIGHT:
            case ThemeMode.TRANSPARENT_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;

            case ThemeMode.DARK:
            case ThemeMode.SHADOW:
            case ThemeMode.GLASS:
            case ThemeMode.GLASS_DARK:
            case ThemeMode.TRANSPARENT_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;

            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    static int getPerfectTheme(String themeMode, String backgroundType) {
        boolean isTransparent = backgroundType != null && "system_wallpaper".equals(backgroundType);

        switch (themeMode) {
            case ThemeMode.TRANSPARENT_LIGHT:
                return isTransparent ? R.style.Theme_Flux_Light : R.style.Theme_Flux_Light;
            case ThemeMode.TRANSPARENT_DARK:
                return isTransparent ? R.style.Theme_Flux_Dark : R.style.Theme_Flux_Dark;

            case ThemeMode.LIGHT:
            case ThemeMode.GLASS_LIGHT:
                return R.style.Theme_Flux_Light;

            case ThemeMode.DARK:
            case ThemeMode.SHADOW:
            case ThemeMode.GLASS:
            case ThemeMode.GLASS_DARK:
                return R.style.Theme_Flux_Dark;

            case ThemeMode.DYNAMIC:
                return R.style.Theme_Flux_Dark;

            default:
                return R.style.Theme_Flux_Dark;
        }
    }
}
