plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    maven { setUrl("https://plugins.gradle.org/m2/") }
}

kotlin {
    val main by sourceSets.getting {
        kotlin.srcDir("buildSrc/src/main/kotlin")
    }
}
