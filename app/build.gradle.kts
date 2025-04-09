import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// 🔁 Lê o local.properties ANTES do bloco android
val localProperties = File(rootDir, "local.properties")
val properties = Properties()
properties.load(FileInputStream(localProperties))

android {
    namespace = "com.example.routeshare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.routeshare"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"


        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY")
            ?: throw GradleException("MAPS_API_KEY not found in local.properties")

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
        compose = true
    }
}

dependencies {

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.android)

    // Gson
    implementation(libs.gson)

    // Location
    implementation(libs.fused.location)

    // Navigation
    implementation(libs.navigation.compose)

    // Maps
    implementation(libs.maps.compose)
    implementation(libs.maps.sdk)
    implementation(libs.play.services.maps.v1910)
    implementation(libs.accompanist.permissions)



    // Icons
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}