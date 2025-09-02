import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    kotlin("jvm")
    id("maven-publish")
    alias(libs.plugins.dokka)
    signing
}

dependencies {
    api(projects.kotlinCdkLib)
}


kotlin {
    explicitApi = ExplicitApiMode.Warning
    jvmToolchain(17)
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        artifact(javadocJar)
        pom {
            name.set("CDK4KT Constructs")
            description.set("Kotlin extended Constructs for CDK.")
            url.set("https://github.com/steamstreet/cdk4kt")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    organization.set("SteamStreet LLC")
                    organizationUrl.set("https://github.com/steamstreet")
                }
            }
            scm {
                url.set("https://github.com/steamstreet/cdk4kt")
            }
        }
    }
}

/**
 * for publishing. Requires the following properties:
 * signing.keyId, signing.password, signing.secretKeyRingFile
 * See: https://docs.gradle.org/current/userguide/signing_plugin.html
 */
signing {
    sign(publishing.publications)
}

tasks.withType<Sign> {
    onlyIf { project.hasProperty("signing.keyId") }
}