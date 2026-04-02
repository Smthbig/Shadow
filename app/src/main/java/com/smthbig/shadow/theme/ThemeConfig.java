package com.smthbig.shadow.theme;

import androidx.appcompat.app.AppCompatDelegate;
import com.smthbig.shadow.R;

final class ThemeConfig {

    private ThemeConfig() {}

    /* ---------- NIGHT MODE ---------- */

    static int getNightMode(String mode) {

        switch (mode) {

            case ThemeMode.LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;

            case ThemeMode.DARK:
            case ThemeMode.SHADOW:
            case ThemeMode.GLASS:
                return AppCompatDelegate.MODE_NIGHT_YES;

            case ThemeMode.SYSTEM:
            case ThemeMode.DYNAMIC:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    /* ---------- THEME RESOLUTION ---------- */

    static int getThemeRes(String mode) {

        switch (mode) {

            case ThemeMode.LIGHT:
                return R.style.Theme_Shadow_Light;

            case ThemeMode.DARK:
                return R.style.Theme_Shadow_Dark;

            case ThemeMode.SHADOW:
                return R.style.Theme_Shadow_Legacy;

            case ThemeMode.GLASS:
                return R.style.Theme_Shadow_Glass;

            case ThemeMode.DYNAMIC:
                return R.style.Theme_Shadow_Dynamic;

            case ThemeMode.SYSTEM:
            default:
                return R.style.Theme_Shadow_Base;
        }
    }
}