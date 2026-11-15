plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tiendnm.zukulock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tiendnm.zukulock"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"
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
            isMinifyEnabled = true
            isShrinkResources = true
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
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)

    testImplementation(libs.junit)
}