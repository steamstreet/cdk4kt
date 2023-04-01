plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.8.10"
    signing
}

kotlin {
    jvm {}

    sourceSets["jvmMain"].apply {
        kotlin.srcDir("build/cdk/wrappers")

        dependencies {
            api(libs.aws.cdk)
            api(libs.kotlin.serialization.json)
            api("org.jetbrains.kotlin:kotlin-reflect")
        }
    }
}

val wrappers = tasks.register<GenerateKotlinWrappers>("generate-wrappers") {
    outputDir.set(File(buildDir, "cdk/wrappers").canonicalPath)
    outputPackage.set("com.steamstreet.cdk.kotlin")
}
tasks["compileKotlinJvm"].dependsOn(wrappers)

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set("CDK4KT")
            description.set("Kotlin DSL wrappers for the AWS CDK.")
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
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = findProperty("sonatypeUsername").toString()
                password = findProperty("sonatypePassword").toString()
            }
        }
        maven {
            name = "space"
            url = uri("https://maven.pkg.jetbrains.space/steamstreet/p/vg/vegasful")

            credentials {
                username = (project.findProperty("steamstreet.space.username") as? String) ?: System.getenv("JB_SPACE_CLIENT_ID")
                password = (project.findProperty("steamstreet.space.password") as? String) ?: System.getenv("JB_SPACE_CLIENT_SECRET")
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