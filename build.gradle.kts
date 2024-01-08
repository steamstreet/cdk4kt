buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

val cdkVersion = findProperty("cdkVersion") as String
val kotlinVersion = findProperty("kotlinVersion") as String
val releaseVersion = findProperty("RELEASE_NAME") as? String

allprojects {
    group = "com.steamstreet"
    version = releaseVersion ?: "$cdkVersion-SNAPSHOT"
}

nexusPublishing {
    repositories {
        sonatype {
            username = findProperty("sonatypeUsername").toString()
            password = findProperty("sonatypePassword").toString()
        }
    }
}