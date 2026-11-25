plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.perfbubble"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.perfbubble"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
    // API nativa do AxManager (axsh). Nada de HTTP local.
    implementation("com.github.fahrez182.Axeron-API:axsh:3.0.0")
}
