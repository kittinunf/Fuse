android {
    defaultConfig {
        applicationId = "com.github.kittinunf.fuse.sample"
    }
}

dependencies {
    val kotlinVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    implementation(project(":fuse"))
    implementation("com.github.kittinunf.fuel:fuel:2.2.1")
}