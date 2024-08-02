buildscript {
    repositories {
        gradlePluginPortal()
        // mavenCentral() include if you need plugins from other repositories
        // google() include if you need plugins from other repositories
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        // other plugin dependencies...
    }
}
plugins {
    alias(libs.plugins.android.library)
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.farsitel.bazaar.updater"
    compileSdk = 31

    buildFeatures {
        aidl = true
    }

    defaultConfig {
        minSdk = 17

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)

}
publishing {
    publications {
        create("release", MavenPublication::class) {
            groupId = "com.farsitel.bazaar"
            artifactId = "updater"
            version = "1.0.0-alpha9"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

