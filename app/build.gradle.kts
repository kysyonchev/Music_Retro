plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.retro.tv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.retro.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.1"
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.material:material")
    implementation("androidx.tv:tv-material:1.1.0")
    implementation("androidx.webkit:webkit:1.12.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
