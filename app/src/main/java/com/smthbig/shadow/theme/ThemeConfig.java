package com.smthbig.shadow.theme;

import androidx.appcompat.app.AppCompatDelegate;
import com.smthbig.shadow.R;
import com.smthbig.shadow.data.FeatureStore;

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
        // 🚀 THEME IS NOW THE ONLY TRUTH
        switch (themeMode) {
            case ThemeMode.TRANSPARENT_LIGHT:
                return R.style.Theme_Shadow_Transparent_Light;
            case ThemeMode.TRANSPARENT_DARK:
                return R.style.Theme_Shadow_Transparent;
            
            case ThemeMode.LIGHT: return R.style.Theme_Shadow_Light;
            case ThemeMode.DARK: return R.style.Theme_Shadow_Dark;
            case ThemeMode.SHADOW: return R.style.Theme_Shadow_Legacy;
            case ThemeMode.GLASS: return R.style.Theme_Shadow_Glass;
            case ThemeMode.GLASS_LIGHT: return R.style.Theme_Shadow_Glass_Light;
            case ThemeMode.GLASS_DARK: return R.style.Theme_Shadow_Glass_Dark;
            case ThemeMode.DYNAMIC: return R.style.Theme_Shadow_Dynamic;
            default: return R.style.Theme_Shadow_Base;
        }
    }
}
