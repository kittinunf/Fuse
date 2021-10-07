plugins {
    kotlin("multiplatform")

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

//    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
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

//dependencies {
//    implementation(libs.kotlinx.serialization.json)
//    implementation(libs.diskCache)
//
//    api(libs.result)
//
//    testImplementation(libs.test.junit)
//}
//
//val sourcesJar by tasks.registering(Jar::class) {
//    from(project.extensions.getByType<SourceSetContainer>()["main"].allSource)
//    archiveClassifier.set("sources")
//}
