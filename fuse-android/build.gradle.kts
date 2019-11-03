dependencies {
    implementation(project(":fuse"))

    val kotlinVersion: String by project
    implementation(kotlin("stdlib", kotlinVersion))
}