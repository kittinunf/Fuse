plugins {
    kotlin("plugin.serialization") version "1.4.0"
}

dependencies {
    val kotlinXSerializationVersion: String by project
    val diskLruCacheVersion: String by project
    val resultVersion: String by project
    val junitVersion: String by project
    val jsonVersion: String by project

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinXSerializationVersion")
    implementation("com.jakewharton:disklrucache:$diskLruCacheVersion")

    api("com.github.kittinunf.result:result:$resultVersion")

    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.json:json:$jsonVersion")
}
