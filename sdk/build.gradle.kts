plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "life.nuggets.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    api("androidx.core:core-ktx:1.12.0")
    api("androidx.appcompat:appcompat:1.6.1")
    api("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // load third-party dependencies
    File("${rootDir}/sdk/libs.txt").forEachLine { api("$it") }
    
    api("com.github.NuggetsLtd:mobile-sdk-android-libs:v3.0.21")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "life.nuggets"
            artifactId = "sdk"
            version = "3.0.21"
            println("sdk: This is executed during the configuration phase.")

            afterEvaluate {
                println("sdk: This is executed during the afterEvaludate phase.")
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
