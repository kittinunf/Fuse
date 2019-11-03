dependencies {
    val kotlinVersion: String by project
    val diskLruCacheVersion: String by project
    val resultVersion: String by project
    val junitVersion: String by project

    implementation(kotlin("stdlib", kotlinVersion))
    implementation("com.jakewharton:disklrucache:$diskLruCacheVersion")

    api("com.github.kittinunf.result:result:$resultVersion")

    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")
    testImplementation("org.json:json:20190722")
}