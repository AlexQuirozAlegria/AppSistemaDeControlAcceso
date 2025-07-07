plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
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
    // Core Android & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle & ViewModel & LiveData (AndroidX Architecture Components)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx) // Provides 'by viewModels()' and other fragment extensions

    // Navigation (AndroidX Navigation Component)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // Or consider 'converter-moshi' for potential performance benefits
    implementation(libs.okhttp) // Explicit for logging interceptor, though often a transitive dep of Retrofit
    implementation(libs.logging.interceptor) // For debugging network requests

    // QR Code Scanning
    implementation(libs.zxing.android.embedded)

    // Project-specific Core module (if 'libs.core' refers to a local module)
    // Ensure 'core' is a descriptive name in libs.versions.toml if it's a third-party library
    implementation(libs.core)

    // Testing
    testImplementation(libs.junit) // Unit tests
    androidTestImplementation(libs.androidx.junit) // Instrumented tests
    androidTestImplementation(libs.androidx.espresso.core) // UI tests

    // ... other dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
}