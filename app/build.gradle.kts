plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "life.nuggets.mobilesdkandroid"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        missingDimensionStrategy("react-native-camera", "general")
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
    api(project(":sdk"))
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "life.nuggets"
            artifactId = "mobilesdkandroid"
            version = "3.0.17"
            println("app: This is executed during the configuration phase.")

            afterEvaluate {
                println("app: This is executed during the afterEvaludate phase.")
                println(components)
                from(components["release"])

                components.forEach { component ->
                    println("Component")
                    println(component.getName())
                }
            }
        }
    }
}
