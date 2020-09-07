android {
    defaultConfig {
        applicationId = "com.github.kittinunf.fuse.sample"
    }
}

dependencies {
    val appCompatVersion: String by project
    val constraintLayoutVersion: String by project
    val fuelVersion: String by project

    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")

    implementation(project(":fuse"))
    implementation(project(":fuse-android"))
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
}
