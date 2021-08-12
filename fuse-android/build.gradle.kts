plugins {
    id("com.android.library")
    kotlin("android")
    id("publication")
}

val artifactGroupId: String by project
group = artifactGroupId

val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir)?.trim().orEmpty()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

val artifactPublishVersion: String by project
version = if (isReleaseBuild) artifactPublishVersion else "master-$gitSha-SNAPSHOT"

android {
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":fuse"))

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
}

val sourcesJar by tasks.registering(Jar::class) {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}
