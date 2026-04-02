import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application") version "8.2.2"
}

// -------------------------------
// 🔐 Load Keystore Properties
// -------------------------------
val keystorePropsFile = rootProject.file("release.properties")
val keystoreProps = Properties()

if (keystorePropsFile.exists()) {
    FileInputStream(keystorePropsFile).use {
        keystoreProps.load(it)
    }
}

val hasValidSigningProps = listOf(
    "storeFile",
    "storePassword",
    "keyAlias",
    "keyPassword"
).all { key -> keystoreProps[key] != null }

val isCI = System.getenv("CI") == "true"

// -------------------------------
// ⚙️ Android Config
// -------------------------------
android {
    namespace = "com.smthbig.shadow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smthbig.shadow"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // -------------------------------
    // 🔐 Signing Configs (FIXED SCOPE)
    // -------------------------------
    signingConfigs {
        if (isCI) {
            create("release") {
                storeFile = file(System.getenv("KEYSTORE_FILE"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        } else if (hasValidSigningProps) {
            create("release") {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    // -------------------------------
    // 🏗 Build Types
    // -------------------------------
    buildTypes {
        release {
            if (hasValidSigningProps || isCI) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
        }
    }

    // -------------------------------
    // ⚠️ Lint
    // -------------------------------
    lint {
        checkReleaseBuilds = false
    }

    // -------------------------------
    // ☕ Java Compatibility
    // -------------------------------
    compileOptions {
        // Java 21 not fully stable on Android toolchain yet
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // -------------------------------
    // 🧩 Features
    // -------------------------------
    buildFeatures {
        viewBinding = true
    }

    // -------------------------------
    // 📦 Packaging
    // -------------------------------
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/kotlinx_coroutines_core.version"
        }
    }
}

// -------------------------------
// ⚠️ Dependency Resolution (Keep Minimal)
// -------------------------------
configurations.all {
    resolutionStrategy {
        // Intentionally empty
    }
}

// -------------------------------
// ⚙️ Compiler Options
// -------------------------------
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

// -------------------------------
// 📚 Dependencies
// -------------------------------
dependencies {
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation("androidx.interpolator:interpolator:1.0.0")
}