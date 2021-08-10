// Main libraries
object Kotlin {

    private const val version = "1.5.10"

    const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
    const val pluginSerialization = "org.jetbrains.kotlin:kotlin-serialization:$version"
}

object Android {

    const val minSdkVersion = 24
    const val targetSdkVersion = 30
    const val compileSdkVersion = 30

    private const val version = "7.0.0-rc01"
    const val plugin = "com.android.tools.build:gradle:$version"
}

object AndroidX {

    object Versions {
        const val junit = "1.1.1"
    }
}

object Serialization {

    private const val version = "1.2.1"

    const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
}

object Cache {

    private const val version = "2.0.2"
    const val diskAndroid = "com.jakewharton:disklrucache:$version"
}

object Result {

    private const val version = "3.1.0"
    const val android = "com.github.kittinunf.result:result:$version"
}


// Test libraries
object JUnit {

    private const val version = "4.13.1"
    private const val jacocoVersion = "0.16.0"

    const val jvm = "junit:junit:$version"
    const val pluginJacoco = "gradle.plugin.com.vanniktech:gradle-android-junit-jacoco-plugin:$jacocoVersion"
}

object Jacoco {
    const val version = "0.8.7"
}

object Robolectric {

    private const val version = "4.4"

    const val jvm = "org.robolectric:robolectric:$version"
}

object GradleNexus {

    private const val version = "1.1.0"

    const val pluginNexus = "io.github.gradle-nexus:publish-plugin:$version"
}
