# Keep all your core logic
-keep class com.smthbig.shadow.** { *; }

# Keep model classes (if any)
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep usage stats related
-keep class android.app.usage.** { *; }

# Prevent stripping of activities
-keep public class * extends android.app.Activity

# Keep Material components
-keep class com.google.android.material.** { *; }