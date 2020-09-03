plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.72"
}

dependencies {
    val kotlinVersion: String by project
    val kotlinXSerializationVersion: String by project
    val diskLruCacheVersion: String by project
    val resultVersion: String by project
    val junitVersion: String by project
    val hamcrestVersion: String by project
    val jsonVersion: String by project

    implementation(kotlin("stdlib", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinXSerializationVersion")
    implementation("com.jakewharton:disklrucache:$diskLruCacheVersion")

    api("com.github.kittinunf.result:result:$resultVersion")

    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.hamcrest:hamcrest-junit:$hamcrestVersion")
    testImplementation("org.json:json:$jsonVersion")
}