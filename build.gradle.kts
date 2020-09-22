import com.android.build.gradle.BaseExtension
import com.jfrog.bintray.gradle.BintrayExtension.GpgConfig
import org.jmailen.gradle.kotlinter.support.ReporterType

buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        val androidGradleVersion: String by project
        val kotlinterVersion: String by project

        classpath("com.android.tools.build:gradle:$androidGradleVersion")
        classpath("org.jmailen.gradle:kotlinter-gradle:$kotlinterVersion")
    }
}

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.jmailen.kotlinter") version "2.1.2"

    jacoco
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

val jvm = setOf("fuse")
val android = setOf("fuse-android")
val sample = setOf("sample")

subprojects {
    val isJvm = project.name in jvm
    val isSample = project.name in sample
    val isAndroid = project.name in android

    if (isJvm) {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            plugin("jacoco")
        }

        jacoco {
            val jacocoVersion: String by project
            toolVersion = jacocoVersion
        }

        val sourcesJar by tasks.registering(Jar::class) {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }

        val doc by tasks.creating(Javadoc::class) {
            isFailOnError = false
            source = sourceSets["main"].allJava
        }
    }

    if (isAndroid) {
        apply {
            plugin("com.android.library")
            plugin("org.jetbrains.kotlin.android")
        }

        configure<BaseExtension> {
            compileSdkVersion(28)

            defaultConfig {
                minSdkVersion(15)
                targetSdkVersion(28)

                versionCode = 1
                versionName = "1.0"

                testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                debug {
                    isTestCoverageEnabled = false
                }

                release {
                    consumerProguardFiles("proguard-rules.pro")
                }
            }

            testOptions {
                unitTests.isReturnDefaultValues = true
                unitTests.isIncludeAndroidResources = true
            }

            val sourcesJar by tasks.registering(Jar::class) {
                from(sourceSets["main"].java.srcDirs)
                archiveClassifier.set("sources")
            }

            val doc by tasks.creating(Javadoc::class) {
                isFailOnError = false
                source = sourceSets["main"].java.sourceFiles
                classpath += files(bootClasspath.joinToString(File.pathSeparator))
                classpath += configurations.compile
            }
        }
    }

    if (isSample) {
        apply {
            plugin("com.android.application")
            plugin("org.jetbrains.kotlin.android")
            plugin("org.jetbrains.kotlin.android.extensions")
        }

        configure<BaseExtension> {
            compileSdkVersion(29)

            defaultConfig {
                minSdkVersion(15)
                targetSdkVersion(29)

                versionCode = 1
                versionName = "1.0"
            }

            buildTypes {
                debug {}

                release {}
            }
        }
    }

    if (!isSample) {
        apply {
            plugin("maven-publish")
            plugin("com.jfrog.bintray")
            plugin("org.jmailen.kotlinter")
        }

        val artifactRepo: String by project
        val artifactName: String by project
        val artifactDesc: String by project
        val artifactUserOrg: String by project
        val artifactUrl: String by project
        val artifactScm: String by project
        val artifactLicenseName: String by project
        val artifactLicenseUrl: String by project

        val artifactPublish: String by project
        val artifactGroupId: String by project
        version = artifactPublish
        group = artifactGroupId

        //publishing
        publishing {
            val javadocJar by tasks.creating(Jar::class) {
                val doc by tasks
                dependsOn(doc)
                from(doc)

                archiveClassifier.set("javadoc")
            }

            val sourcesJar by tasks

            publications {
                register(project.name, MavenPublication::class) {
                    if (isAndroid) {
                        artifact("$buildDir/outputs/aar/${project.name}-release.aar") {
                            builtBy(tasks.getByPath("assemble"))
                        }
                    } else {
                        from(components["java"])
                    }
                    artifact(sourcesJar)
                    artifact(javadocJar)
                    groupId = artifactGroupId
                    artifactId = project.name
                    version = artifactPublish

                    pom {
                        name.set(project.name)
                        description.set(artifactDesc)

                        packaging = if (isAndroid) "aar" else "jar"
                        url.set(artifactUrl)

                        licenses {
                            license {
                                name.set(artifactLicenseName)
                                url.set(artifactLicenseUrl)
                            }
                        }

                        developers {
                            developer {
                                name.set("kittinunf")
                            }

                            developer {
                                name.set("BabeDev")
                            }
                            developer {
                                name.set("beylerian")
                            }
                            developer {
                                name.set("zalewskise")
                            }
                        }

                        contributors {
                            // https://github.com/kittinunf/Result/graphs/contributors
                        }

                        scm {
                            url.set(artifactUrl)
                            connection.set(artifactScm)
                            developerConnection.set(artifactScm)
                        }
                    }
                }
            }
        }

        // bintray
        bintray {
            user = findProperty("BINTRAY_USER") as? String
            key = findProperty("BINTRAY_KEY") as? String
            setPublications(project.name)
            publish = true
            pkg.apply {
                repo = artifactRepo
                name = artifactName
                desc = artifactDesc
                userOrg = artifactUserOrg
                websiteUrl = artifactUrl
                vcsUrl = artifactUrl
                setLicenses(artifactLicenseName)
                version.apply {
                    name = artifactPublish
                    gpg(delegateClosureOf<GpgConfig> {
                        sign = true
                        passphrase = System.getenv("GPG_PASSPHRASE") ?: ""
                    })
                }
            }
        }

        tasks.withType<JacocoReport> {
            reports {
                html.isEnabled = true
                xml.isEnabled = true
                csv.isEnabled = false
            }
        }

        kotlinter {
            reporters = arrayOf(ReporterType.plain.name, ReporterType.checkstyle.name)
        }
    }
}

fun <T> NamedDomainObjectContainer<T>.release(configure: T.() -> Unit) = getByName("release", configure)
fun <T> NamedDomainObjectContainer<T>.debug(configure: T.() -> Unit) = getByName("debug", configure)
fun <T> NamedDomainObjectContainer<T>.all(configure: T.() -> Unit) = getByName("all", configure)
