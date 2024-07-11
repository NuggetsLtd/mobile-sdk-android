// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.android.library") version "8.2.2" apply false
    id("maven-publish")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "life.nuggets"
            artifactId = "mobilesdk"
            version = "1.1.97"
            println("This is executed during the configuration phase.")

            afterEvaluate {
                println("This is executed during the afterEvaludate phase.")
                println(components)
            }
        }
    }
}
