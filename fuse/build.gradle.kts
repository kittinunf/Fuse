plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("publication")
}

dependencies {
    implementation(Serialization.json)
    implementation(Cache.diskAndroid)

    api(Result.android)

    testImplementation(JUnit.jvm)
}
