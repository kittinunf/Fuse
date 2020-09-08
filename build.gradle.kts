import com.android.build.gradle.BaseExtension
import org.gradle.api.publish.maven.MavenPom
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
    kotlin("jvm") version "1.4.0"
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

        val sourcesJar by tasks.registering(Jar::class) {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }

        val doc by tasks.creating(Javadoc::class) {
            isFailOnError = false
            source = sourceSets["main"].allJava
        }

        jacoco {
            val jacocoVersion: String by project
            toolVersion = jacocoVersion
        }

        tasks.withType<JacocoReport> {
            reports {
                html.isEnabled = false
                xml.isEnabled = true
                csv.isEnabled = false
            }
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
        val artifactVersion: String by project
        val artifactGroup: String by project

        version = artifactVersion
        group = artifactGroup

        bintray {
            user = findProperty("BINTRAY_USER") as? String
            key = findProperty("BINTRAY_KEY") as? String
            setPublications(project.name)
            with(pkg) {
                repo = "maven"
                name = "Fuse"
                desc = "The simple generic LRU memory/disk cache for Android written in Kotlin."
                userOrg = "kittinunf"
                websiteUrl = "https://github.com/kittinunf/Fuse"
                vcsUrl = "https://github.com/kittinunf/Fuse"
                setLicenses("MIT")
                with(version) {
                    name = artifactVersion
                }
            }
        }

        val javadocJar by tasks.creating(Jar::class) {
            val doc by tasks
            dependsOn(doc)
            from(doc)

            archiveClassifier.set("javadoc")
        }

        fun MavenPom.addDependencies() = withXml {
            val dependenciesNode = asNode().appendNode("dependencies")
            configurations["implementation"].allDependencies.forEach {
                dependenciesNode.appendNode("dependency").apply {
                    appendNode("groupId", it.group)
                    appendNode("artifactId", it.name)
                    appendNode("version", it.version)
                }
            }
        }

        publishing {
            val sourcesJar by tasks

            publications {
                register(project.name, MavenPublication::class) {
                    if (project.hasProperty("android")) {
                        artifact("$buildDir/outputs/aar/${project.name}-release.aar") {
                            builtBy(tasks.getByPath("assemble"))
                        }
                    } else {
                        from(components["java"])
                    }
                    artifact(sourcesJar)
                    artifact(javadocJar)

                    groupId = artifactGroup
                    artifactId = project.name
                    version = artifactVersion

                    pom {
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("http://www.opensource.org/licenses/mit-license.php")
                            }
                        }
                    }

                    if (project.hasProperty("android")) {
                        pom.addDependencies()
                    }
                }
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
