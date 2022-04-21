plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("com.android.library")
//    java
    jacoco


    id("publication")
}

val artifactGroupId: String by project
group = artifactGroupId

val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir)?.trim().orEmpty()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

val artifactPublishVersion: String by project
version = if (isReleaseBuild) artifactPublishVersion else "master-$gitSha-SNAPSHOT"

kotlin {
    jvm()
    ios()
    iosSimulatorArm64()
    android()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.time)
                api(libs.result)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotlin.test)
            }
        }

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }

        val iosSimulatorArm64Main by getting {
            val iosMain by getting
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Test by getting {
            val iosTest by getting
            dependsOn(iosTest)
        }

        val androidMain by getting {
            dependsOn(jvmMain)
        }

        val androidTest by getting {
            dependencies {
                implementation(libs.bundles.android.test)
                implementation(libs.kotlin.test.junit)
            }
        }
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
        }

        getByName("androidTest") {
            manifest.srcFile("src/androidTest/AndroidManifest.xml")
            java.srcDirs("src/androidTest/kotlin")
            res.srcDirs("src/androidTest/res")
        }
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks {
    withType<JacocoReport> {
        group = "Reporting"
        description = "Generate Jacoco coverage reports."

        val jvmTest by getting
        dependsOn(jvmTest)

        val classFiles = File("$buildDir/classes/kotlin/jvm/main").walkBottomUp().toSet()
        classDirectories.setFrom(classFiles)
        sourceDirectories.setFrom(files(arrayOf("$projectDir/src/commonMain")))
        executionData.setFrom(files("$buildDir/jacoco/jvmTest.exec"))

        reports {
            xml.required.set(true)

            html.required.set(true)
            html.outputLocation.set(buildDir.resolve("reports"))

            csv.required.set(false)
        }
    }
}
