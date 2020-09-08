android {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    implementation(project(":fuse"))

    val junitVersion: String by project
    val robolectricVersion: String by project
    val kotlinVersion: String by project

    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
}
