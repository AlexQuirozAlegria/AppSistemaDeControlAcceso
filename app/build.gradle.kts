plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //retrofit
    // Use your Kotlin version or latest compatible
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.androidqr"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.androidqr"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Retrofit
    implementation(libs.retrofit) // Check for the latest version
    implementation(libs.converter.gson) // Or converter-moshi
    // OkHttp (usually included with Retrofit, but good to be explicit for logging)
    implementation(libs.okhttp) // Check for the latest version
    implementation(libs.logging.interceptor) // For debugging network requests

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.zxing.android.embedded)
    implementation(libs.core)

}