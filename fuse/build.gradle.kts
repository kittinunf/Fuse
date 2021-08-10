plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("publication")
}

val artifactGroupId: String by project
group = artifactGroupId

val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir)?.trim().orEmpty()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

val artifactPublishVersion: String by project
version = if (isReleaseBuild) artifactPublishVersion else "master-$gitSha-SNAPSHOT"

dependencies {
    implementation(Serialization.json)
    implementation(Cache.diskAndroid)

    api(Result.android)

    testImplementation(JUnit.jvm)
}
