// app/build.gradle.kts (App Level)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.neonclock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.neonclock"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM（Bill of Materials）でバージョンを一括管理
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    // Compose コア
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material 3
    implementation(libs.androidx.material3)

    // Activity Compose
    implementation(libs.androidx.activity.compose)

    // Lifecycle Runtime Compose（LocalLifecycleOwner / repeatOnLifecycle）
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Core KTX（WindowCompat などのユーティリティ）
    implementation(libs.androidx.core.ktx)

    // デバッグ用ツール（開発時のみ）
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
