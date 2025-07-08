// build.gradle (Module: app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
    // NUEVO: Añadir el plugin kotlin-parcelize
    id("kotlin-parcelize")
    // Opcional: Si usas Safe Args, asegúrate de que también esté aquí
    // id("androidx.navigation.safeargs.kotlin")
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
    implementation(libs.androidx.fragment.ktx)

    // Navigation (AndroidX Navigation Component)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // QR Code Scanning
    implementation(libs.zxing.android.embedded)

    //Serialization
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.retrofit2KotlinxSerializationConverter)

    // Project-specific Core module (if 'libs.core' refers to a local module)
    implementation(libs.core)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
