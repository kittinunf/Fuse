import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.Properties

plugins {
    `maven-publish`
    signing
}

ext["signing.key"] = null
ext["signing.password"] = null
ext["sonatype.username"] = null
ext["sonatype.password"] = null

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.key"] = System.getenv("SIGNING_KEY")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["sonatype.username"] = System.getenv("SONATYPE_USERNAME")
    ext["sonatype.password"] = System.getenv("SONATYPE_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

val isReleaseBuild: Boolean
    get() = properties.containsKey("release")

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "sonatype"
                url = uri(
                    if (isReleaseBuild) {
                        "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                    } else {
                        "https://oss.sonatype.org/content/repositories/snapshots"
                    }
                )

                credentials {
                    username = getExtraString("sonatype.username")
                    password = getExtraString("sonatype.password")
                }
            }
        }

        publications.register<MavenPublication>(project.name) {
            val javadocJar by tasks.registering(Jar::class) {
                archiveClassifier.set("javadoc")
            }

            // Configure all publications
            val artifactName: String by project
            val artifactDesc: String by project
            val artifactUrl: String by project
            val artifactScm: String by project
            val artifactUserOrg: String by project
            val artifactLicenseName: String by project
            val artifactLicenseUrl: String by project

            artifactId = project.name

            if (project.hasProperty("android")) {
                artifact("$buildDir/outputs/aar/${project.name}-release.aar") {
                    builtBy(tasks.getByPath("assemble"))
                }
            } else {
                from(components["java"])
            }

            val sourcesJar by tasks.getting

            artifact(javadocJar)
            artifact(sourcesJar)

            // Provide artifacts information requited by Maven Central
            pom {
                name.set(artifactName)
                description.set(artifactDesc)
                url.set(artifactUrl)

                licenses {
                    license {
                        name.set(artifactLicenseName)
                        url.set(artifactLicenseUrl)
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set(artifactUserOrg)
                    }
                }
                contributors {
                    contributor {
                        name.set("zalewskise")
                    }
                    contributor {
                        name.set("eschlenz")
                    }
                    contributor {
                        name.set("babedev")
                    }
                    contributor {
                        name.set("nicolashaan")
                    }
                }
                scm {
                    connection.set(artifactScm)
                    developerConnection.set(artifactScm)
                    url.set(artifactUrl)
                }
            }

            fun MavenPom.addDependencies() = withXml {
                asNode().appendNode("dependencies").let { depNode ->
                    configurations.getByName("implementation").allDependencies.forEach {
                        depNode.appendNode("dependency").apply {
                            appendNode("groupId", it.group)
                            appendNode("artifactId", it.name)
                            appendNode("version", it.version)
                        }
                    }
                }
            }

            if (project.hasProperty("android")) {
                pom.addDependencies()
            }
        }
    }

    signing {
        val signingKey = project.ext["signing.key"] as? String
        val signingPassword = project.ext["signing.password"] as? String
        if (signingKey == null || signingPassword == null) return@signing

        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
