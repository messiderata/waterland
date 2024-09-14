plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.waterlanders"
    compileSdk = 34

    signingConfigs {
        // Define your release signing config
        create("release") {
            storeFile = file("/home/messi/Desktop/Water/waterland/waterlanders/aly.jks")
            storePassword = "alyInWaterland.123"
            keyAlias = "Aly"
            keyPassword = "alyInWaterland.123"
        }

        getByName("release") {
            // Keep the default debug signing config or specify your own debug signing config here if needed
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.waterlanders"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.google.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.com.google.firebase.firebase.auth)
    implementation(libs.facebook.android.sdk)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.storage)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.lottie)
    androidTestImplementation(libs.startup)
    implementation (libs.jbcrypt)
}
