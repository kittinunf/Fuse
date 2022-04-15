plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    java
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

        val iosMain by getting
        val iosTest by getting

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
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