buildscript {
    dependencies {
        classpath("software.amazon.awssdk:sso:2.17.295")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

val cdkVersion = findProperty("cdkVersion") as String
val kotlinVersion = findProperty("kotlinVersion") as String
val releaseVersion = findProperty("releaseVersion") as? String

println("Release version: ${releaseVersion}")

allprojects {
    group = "com.steamstreet"
    version = releaseVersion ?: "$cdkVersion-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}