buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    alias(libs.plugins.nexus.publish)
}

val cdkVersion = findProperty("cdkVersion") as String
val releaseVersion = findProperty("RELEASE_NAME") as? String

allprojects {
    group = "com.steamstreet"
    version = releaseVersion?.removePrefix("v") ?: "$cdkVersion-SNAPSHOT"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username = findProperty("mavenCentralUsername").toString()
            password = findProperty("mavenCentralPassword").toString()
        }
    }
}