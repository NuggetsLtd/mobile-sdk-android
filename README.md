<p align="center">
  <a href="https://nuggets.life">
    <img src="./assets/nuggets-logo.svg" alt="Nuggets Ltd" width="250">
  </a>
</p>

# Nuggets Mobile SDK (Android)
Mobile (Android) SDK for interaction with the Nuggets Platform.

This SDK provides an Identity Wallet for Self-Sovereign Identity (SSI).
# Nuggets Mobile SDK – Android Full Integration Guide

This document lists ALL changes needed to integrate the Nuggets Mobile SDK into a fresh Android host app.

---
## 0. Overview & Architecture (Why These Steps Exist)
- Delivery: The SDK is published via JitPack as a multi-module Gradle artifact (plus biometric/iProov components).
- React Native Internals: Internally leverages React Native style modules; some transitive dependencies inflate method count (hence multidex + potential code shrinking considerations).
- Presentation: You typically launch an exported SDK `Activity` (`NuggetsSDKActivity`) from your host UI.
- Isolation: Keeping flows in a separate activity avoids polluting your navigation stack and eases lifecycle separation.
- Permissions: Start broad while integrating; prune truly unused ancillary permissions; core camera + NFC + liveness permissions are REQUIRED.
- Jetifier: Ensures legacy support libraries inside dependencies are migrated to AndroidX at build time.
- Biometrics & Liveness: iProov repository IS REQUIRED for core Nuggets identity flows (do not remove).

---
## 1. Create a New Project
Android Studio → New Project → Empty Activity
1. Name: `WrappingAppDemo` (example)
2. Package: `com.example.wrappingappdemo`
3. Minimum SDK: API 24
4. Language: Kotlin
5. Finish

(Why: API 24 keeps method count manageable while meeting modern TLS / security baseline; raising minSdk may further reduce multidex pressure.)

---
## 2. Add Required Repositories (Root `settings.gradle.kts`)
Add JitPack (required) and iProov (REQUIRED – liveness dependency):
```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://raw.githubusercontent.com/iProov/android/master/maven/") // REQUIRED for iProov liveness
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://raw.githubusercontent.com/iProov/android/master/maven/") // do NOT remove – SDK depends on iProov
    }
}

rootProject.name = "WrappingAppDemo"
include(":app")
```
Groovy variant updated similarly.
Why:
- JitPack serves the Nuggets SDK artifacts directly from Git tags.
- iProov feed required for mandatory liveness / biometric flows.

---
## 3. `gradle.properties`
Ensure Jetifier is enabled (helps with some transitive AndroidX migrations):
```
android.enableJetifier=true
```
(If already present in a corporate standards file, do not duplicate.)

Recommended additional hardening toggles (optional – enable later once integrated):
```
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
android.defaults.buildfeatures.buildconfig=true
android.nonTransitiveRClass=true
# Enable if you want faster incremental builds (requires plugin 8+)
android.defaults.buildfeatures.resvalues=true
```

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
Why: Ensure the SDK activity inherits predictable default theming; host can further override brand palette.

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
        android:name=".App"
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
Prune only non-core sensitive permissions before release (e.g. location if not required). DO NOT remove CAMERA, NFC, RECORD_AUDIO, USE_BIOMETRIC / USE_FINGERPRINT – they are required for Nuggets SDK liveness + document flows.

### 5.1 Permission Rationale Table
| Permission | Needed For | Status |
|------------|------------|--------|
| INTERNET | Network calls | Required |
| CAMERA | Document / face capture | Required |
| RECORD_AUDIO | Liveness / iProov session (audio component) | Required |
| USE_BIOMETRIC / USE_FINGERPRINT | Biometric auth convenience & fallback | Required |
| NFC | Reading NFC-enabled IDs / ePassports | Required |
| POST_NOTIFICATIONS | Out-of-app alerts (Android 13+) | Optional (remove if no notifications) |
| FOREGROUND_SERVICE | Long-running secure tasks | Optional (keep if background actions used) |
| WAKE_LOCK | Prevent sleep mid critical flow | Optional (profile necessity) |
| ACCESS_COARSE/FINE_LOCATION | Geo-based verification | Optional (only if geo required) |
| READ/WRITE_EXTERNAL_STORAGE | Legacy file writes | Optional (often removable >= API 33) |
| DOWNLOAD_WITHOUT_NOTIFICATION | Silent asset fetch | Optional |

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
- `multiDexEnabled`: Required due to transitive RN / crypto libs; remove only after R8 shrink audit shows <64K methods.
- Excluding `react-native-camera-mlkit` reduces size if not using MLKit camera features.
- Conceal provides lightweight encryption support needed by internal modules.
- Keep pinned Compose BOM version to avoid mismatched runtime / compiler extension.

### 6.1 Optional Build Optimizations
Inside `android { }`:
```kotlin
packagingOptions { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
```
Enable only after initial integration to simplify first run debugging.

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

Classic XML variant (if not using Compose):
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.launchButton).setOnClickListener {
            startActivity(Intent(this, life.nuggets.nuggetssdk.NuggetsSDKActivity::class.java))
        }
    }
}
```

---
## 8. Runtime Permissions (Strategy)
Request at the moment of first use (core required capabilities must still be granted for SDK success).
| Capability | Permissions (typical) | Request Timing | Required |
|------------|-----------------------|----------------|----------|
| Camera capture | CAMERA | Just before opening capture flow | Yes |
| Audio / Liveness | RECORD_AUDIO | Before initiating liveness step | Yes |
| Biometric auth | USE_BIOMETRIC / USE_FINGERPRINT | System prompt on auth screen | Yes (for full experience) |
| NFC scanning | NFC | Prior to NFC scanning (no runtime dialog on many devices) | Yes |
| Notifications | POST_NOTIFICATIONS (API 33+) | After user opt-in context screen | No |
| Location (if used) | ACCESS_COARSE/FINE_LOCATION | On feature entry needing geo context | No |

Gracefully handle denial: show rationale & give re-try path directing to App Settings when permanently denied.

---
## 9. Optional R8 / ProGuard Rules
Add if you enable minification and see class stripping issues:
```
-keep class com.facebook.react.** { *; }
-dontwarn com.facebook.react.**
-keep class life.nuggets.** { *; }
-dontwarn life.nuggets.**
```
Refine once official ruleset is published. Avoid over-broad `-keep class **` which harms shrinking.

---
## 10. Validation Checklist
| Step | Verify |
|------|--------|
| Build sync | No unresolved dependencies |
| Launch button | SDK Activity displays UI |
| Orientation change | No crash / state restored |
| Core permissions | CAMERA, RECORD_AUDIO, NFC, BIOMETRIC granted path tested |
| Optional permissions | Not requested unless feature used |
| Release (minify on) | No missing symbols |
| Method count | Below 64K only if multidex removed |

---
## 11. Updating the SDK
1. Read consumer CHANGELOG.
2. Bump version in dependency line.
3. Sync Gradle, rebuild.
4. Regression test critical identity & NFC (if used) flows.
Rollback: revert version line and sync again (Gradle caches prior artifacts).

---
## 12. Support
Provide: SDK version, device model, Android version, reproduction steps, minimal Logcat excerpt.

---
## 13. Troubleshooting
| Symptom | Likely Cause | Remedy |
|---------|--------------|--------|
| Activity not found | Dependency not synced / wrong artifact version | Re-sync Gradle; verify coordinate spelling |
| Crash on launch (NoClassDefFoundError) | ProGuard/R8 removed class | Add `-keep` rule for missing package |
| Permission denied crash | Runtime permission not requested | Add permission request flow before invoking SDK step |
| Blank screen | Theme conflict / missing resources | Ensure using Material3 theme & no custom night mode override breaking RN surface |
| Dex overflow without multidex | `multiDexEnabled` false | Re-enable multidex or shrink aggressively |
| Slow cold start | Debug variant + no shrinking | Measure after enabling minify in release |

---
## 14. Production Hardening
- Remove unused permissions (especially location, storage, notifications).
- Enable R8 (`minifyEnabled true`) and verify no stripped classes.
- Turn on Play Integrity / SafetyNet (if required by business logic outside SDK).
- Monitor memory & jank (Android Studio Profiler) during identity flow.
- Verify dark mode compatibility.
- Add Crash / ANR monitoring (e.g., Firebase Crashlytics) around SDK entrypoints.

---
## 15. Quick Reference (Minimal Permission Set)
Minimum REQUIRED for Nuggets SDK core flows (document + liveness + NFC):
- INTERNET
- CAMERA
- RECORD_AUDIO
- USE_BIOMETRIC (and/or USE_FINGERPRINT)
- NFC
(Optional: POST_NOTIFICATIONS, LOCATION, STORAGE, WAKE_LOCK, FOREGROUND_SERVICE, DOWNLOAD_WITHOUT_NOTIFICATION only if your product use-cases demand them.)

---
> Remove any OPTIONAL permissions or repository endpoints not required; retain CAMERA, NFC, RECORD_AUDIO, BIOMETRIC, iProov repository – they are mandatory for Nuggets SDK.
