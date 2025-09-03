<p align="center">
  <a href="https://nuggets.life">
    <img src="./assets/nuggets-logo.svg" alt="Nuggets Ltd" width="250">
  </a>
</p>

# Nuggets Mobile SDK (Android)
Mobile (Android) SDK for interaction with the Nuggets Platform.

This SDK provides an Identity Wallet for Self-Sovereign Identity (SSI).
# Nuggets Mobile SDK – Android Full Integration Guide

This document lists ALL changes needed to integrate the Nuggets Mobile SDK into a fresh Android host app. Use the Quick Integration guide if you only need basics.

---
## 1. Create a New Project
Android Studio → New Project → Empty Activity
1. Name: `WrappingAppDemo` (example)
2. Package: `com.example.wrappingappdemo`
3. Minimum SDK: API 24
4. Language: Kotlin
5. Finish

---
## 2. Add Required Repositories (Root `settings.gradle.kts`)
Add JitPack (required) and iProov:
```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://raw.githubusercontent.com/iProov/android/master/maven/") // optional
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://raw.githubusercontent.com/iProov/android/master/maven/") // remove if not using biometrics
    }
}

rootProject.name = "WrappingAppDemo"
include(":app")
```
Groovy variant:
```groovy
pluginManagement {
  repositories {
    google(); mavenCentral(); gradlePluginPortal(); maven { url 'https://jitpack.io' }; maven { url 'https://raw.githubusercontent.com/iProov/android/master/maven/' }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google(); mavenCentral(); maven { url 'https://jitpack.io' }; maven { url 'https://raw.githubusercontent.com/iProov/android/master/maven/' }
  }
}
```

---
## 3. `gradle.properties`
Ensure Jetifier is enabled (helps with some transitive AndroidX migrations):
```
android.enableJetifier=true
```
(If already present in a corporate standards file, do not duplicate.)

---
## 4. Module Theme Setup (`app/src/main/res/values/themes.xml`)
Start minimal:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.WrappingAppDemo" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```
Then refine to Material 3 with basic color overrides:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.WrappingAppDemo" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/purple_200</item>
        <item name="colorPrimaryDark">@color/teal_200</item>
        <item name="colorAccent">@color/black</item>
        <item name="colorSecondary">@color/white</item>
        <item name="android:statusBarColor">@color/teal_700</item>
    </style>
</resources>
```
Adjust colors to match host branding.

---
## 5. Android Manifest (`app/src/main/AndroidManifest.xml`)
Add only the permissions/features you actually need. Start broad, then prune:
```xml
<manifest ...>
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.USE_FINGERPRINT" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".App"   <!-- optional if you add an Application class -->
        android:allowBackup="false"
        android:label="@string/app_name"
        android:theme="@style/Theme.WrappingAppDemo">

        <!-- Background actions service (from RN background-actions lib) -->
        <service
            android:name="com.asterinet.react.bgactions.RNBackgroundActionsTask"
            android:foregroundServiceType="shortService" />

        <!-- SDK Activity (exported class from the SDK) -->
        <activity android:name="life.nuggets.nuggetssdk.NuggetsSDKActivity" />

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```
Prune unused sensitive permissions before release (e.g. location if not required). For API 33+ request POST_NOTIFICATIONS at runtime.

---
## 6. Module `build.gradle.kts` (App)
Enable Compose + add SDK dependency + version tuning. Set multidex (the primary required tweak for large method counts). Other fields (namespace, versions) come from the Android Studio template.
```kotlin
android {
    namespace = "com.example.wrappingappdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wrappingappdemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true // Required: SDK + RN exceed 64K methods
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }
}

dependencies {
    // Compose BOM + core UI
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")

    // VERIFIED AndroidX versions (do not bump without compatibility confirmation)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Nuggets SDK (multi-module coordinate style)
    implementation("com.github.NuggetsLtd.mobile-sdk-android:sdk:v3.0.22") {
        exclude(group = "androidx.lifecycle")
        exclude(group = "com.github.NuggetsLtd.mobile-sdk-android-libs", module = "react-native-camera-mlkit")
    }

    implementation("com.facebook.conceal:conceal:1.1.3@aar")
}
```
Notes:
- With minSdk 24 you do NOT need the `androidx.multidex:multidex` runtime dependency; just `multiDexEnabled = true`.
- Remove `multiDexEnabled` only if you shrink & confirm method count < 64K (unlikely with RN + SDK).
- Keep excludes unless you intentionally add those modules.

---
## 7. Example `MainActivity.kt`
A Compose-based host screen with button launching the SDK Activity (plus a simple uncaught exception handler – optional).
```kotlin
package com.example.wrappingappdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GlobalExceptionHandler", "Uncaught exception in thread ${thread.name}", throwable)
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }

    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Host App", modifier = Modifier.padding(bottom = 16.dp))
            Button(onClick = {
                val intent = Intent(
                    this@MainActivity,
                    life.nuggets.nuggetssdk.NuggetsSDKActivity::class.java
                ).apply {
                    putExtra("env", "staging") // optional environment override
                }
                startActivity(intent)
            }) { Text("Launch Nuggets SDK") }
        }
    }
}
```
If the exported activity name differs (e.g. `NuggetsActivity`), use IDE auto-complete after syncing.

---
## 8. Runtime Permissions
Request at runtime for Camera, Audio, Notifications (API 33+), and potentially external storage (scoped storage changes may remove need). Sample minimal request (not shown here) should be implemented before launching flows needing them.

---
## 9. Optional R8 / ProGuard Rules
Add if you enable minification and see class stripping issues:
```
-keep class com.facebook.react.** { *; }
-dontwarn com.facebook.react.**
-keep class life.nuggets.** { *; }
-dontwarn life.nuggets.**
```
Refine once official ruleset is published.

---
## 10. Validation Checklist
| Step | Verify |
|------|--------|
| Build sync | No unresolved dependencies |
| Launch button | SDK Activity displays UI |
| Orientation change | No crash / state restored |
| Permissions | Requested only when needed |
| Release (minify on) | No missing symbols |

---
## 11. Updating the SDK
1. Read consumer CHANGELOG.
2. Bump version in dependency line.
3. Sync Gradle, rebuild.
4. Regression test critical identity & NFC (if used) flows.

---
## 12. Support
Provide: SDK version, device model, Android version, reproduction steps, minimal Logcat excerpt.

---
> Remove any permissions or repository endpoints not required by your deployment before submitting to production stores.
