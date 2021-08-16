plugins {
    id("com.android.application")
    kotlin("android")
}

repositories {
    jcenter()
    mavenCentral()
    google()
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "com.github.kittinunf.fuse.sample"
        minSdk = 24
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.constraintLayout)

    implementation(libs.fuel)
    implementation(project(":fuse"))
    implementation(project(":fuse-android"))
}
