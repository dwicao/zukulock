plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tiendnm.zukulock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tiendnm.zukulock"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing via env vars / gradle properties (used by GitHub CI).
    // CI sets: ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD,
    //          ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD
    // Local builds without these fall back to the debug key.
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
                ?: project.findProperty("ANDROID_KEYSTORE_PATH") as String?
            val storePass = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                ?: project.findProperty("ANDROID_KEYSTORE_PASSWORD") as String?
            val alias = System.getenv("ANDROID_KEY_ALIAS")
                ?: project.findProperty("ANDROID_KEY_ALIAS") as String?
            val keyPass = System.getenv("ANDROID_KEY_PASSWORD")
                ?: project.findProperty("ANDROID_KEY_PASSWORD") as String?
            if (!keystorePath.isNullOrEmpty()) {
                storeFile = file(keystorePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            val hasReleaseKey = !((System.getenv("ANDROID_KEYSTORE_PATH")
                ?: project.findProperty("ANDROID_KEYSTORE_PATH") as String?).isNullOrEmpty())
            signingConfig = if (hasReleaseKey) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)
}