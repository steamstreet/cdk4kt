import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    kotlin("jvm")
    id("maven-publish")
    alias(libs.plugins.dokka)
    signing
}

dependencies {
    api(libs.aws.cdk)
    api(libs.kotlin.reflect)
    api(libs.jackson)

    testImplementation(libs.kotlin.test)
}

kotlin {
    explicitApi = ExplicitApiMode.Warning
    jvmToolchain(17)
}

java.sourceSets["main"].java {
    srcDir("build/cdk/wrappers")
}

val wrappers = tasks.register<GenerateKotlinWrappers>("generate-wrappers") {
    outputDir.set(layout.buildDirectory.file("cdk/wrappers").orNull?.asFile?.canonicalPath)
    outputPackage.set("com.steamstreet.cdk.kotlin")
}

tasks.named("compileKotlin") {
    dependsOn(wrappers)
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val sourcesJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        artifact(sourcesJar)
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
tasks.test {
    useJUnitPlatform()
}