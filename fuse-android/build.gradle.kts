dependencies {
    implementation(project(":fuse"))

    val junitVersion: String by project
    val robolectricVersion: String by project
    val kotlinVersion: String by project

    implementation(kotlin("stdlib", kotlinVersion))

    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
}