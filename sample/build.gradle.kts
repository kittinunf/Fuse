plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
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
    val appCompatVersion: String by project
    val constraintLayoutVersion: String by project
    val fuelVersion: String by project

    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")

    implementation(project(":fuse"))
    implementation(project(":fuse-android"))
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
}
