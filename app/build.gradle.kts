import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application") version "8.2.2"
}

val keystorePropsFile = rootProject.file("release.properties")
val keystoreProps = Properties()

if (keystorePropsFile.exists()) {
    FileInputStream(keystorePropsFile).use {
        keystoreProps.load(it)
    }
}

val hasValidSigningProps =
    listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
        .all { key -> keystoreProps[key] != null }

android {
    namespace = "com.smthbig.shadow"
    compileSdk = 34

    lint {
        checkReleaseBuilds = false
    }

    signingConfigs {
        if (hasValidSigningProps) {
            create("release") {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

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

    compileOptions {
        // ⚠️ Java 21 declared, but safe fallback behavior
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            if (hasValidSigningProps) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/kotlinx_coroutines_core.version"
        }
    }
}

/* 🔥 REMOVE forced resolution — causing hidden conflicts */
configurations.all {
    resolutionStrategy {
        // KEEP EMPTY unless absolutely needed
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation("androidx.interpolator:interpolator:1.0.0")
}