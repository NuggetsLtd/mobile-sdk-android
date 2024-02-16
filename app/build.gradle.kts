plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "life.nuggets.mobilesdkandroid"
    compileSdk = 34

    defaultConfig {
//         applicationId = "life.nuggets.mobilesdkandroid"
        minSdk = 24
        targetSdk = 34
        // versionCode = 1
        // versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // load third-party dependencies
    File("${rootDir}/NuggetsSDKAndroid/libs.txt").forEachLine { api("$it") }
    
    api("com.github.NuggetsLtd:mobile-sdk-android-libs:v0.0.59")
    // api(project(":NuggetsSDKAndroid"))
}
