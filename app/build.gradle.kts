plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
}

android {
    namespace = "com.wisesaint"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wisesaint"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding=true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase Authentication
    implementation(libs.firebase.auth.v2211)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.firebase.bom)

    // RecyclerView for displaying chat messages
    implementation (libs.androidx.recyclerview)

    // Kotlin coroutines for handling the message streaming
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

    // Material Design (optional, for UI components)
    implementation (libs.material.v190)

    //Latex
//    implementation ("com.github.gregcockroft:AndroidMath:ALPHA")

    // Markwon for Markdown rendering
    implementation ("io.noties.markwon:core:4.6.2")

    // Markwon KaTeX for LaTeX rendering
    implementation ("io.noties.markwon:ext-latex:4.6.2")
    implementation ("io.noties.markwon:inline-parser:4.6.2")

    //Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // OkHttp
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    // Coroutines
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.kotlinx.coroutines.core)

    // ViewModel
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)

}