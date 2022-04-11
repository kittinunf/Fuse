enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "Fuse"

includeBuild("plugins")

include(
    ":fuse",
    ":fuse-android",
    ":sample"
)

pluginManagement {

    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }

    val kotlinVersion = rootDir.resolve("gradle/libs.versions.toml").reader().use { java.util.Properties().apply { load(it) } }
        .getProperty("kotlin")
        .removeSurrounding("\"")
    val androidGradleVersion = rootDir.resolve("gradle/libs.versions.toml").reader().use { java.util.Properties().apply { load(it) } }
        .getProperty("androidGradle")
        .removeSurrounding("\"")

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("android") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.android.library") version androidGradleVersion
        id("com.android.application") version androidGradleVersion
    }
}
