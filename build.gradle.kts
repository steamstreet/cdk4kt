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
val releaseVersion = findProperty("releastVersion") as? String

allprojects {
    group = "com.steamstreet"
    version = releaseVersion ?: "$cdkVersion-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}


plugins {
//    kotlin("jvm") version "1.7.22" apply false
//    kotlin("plugin.serialization") version "1.7.22" apply false
//    kotlin("multiplatform") version "1.7.22" apply false
}
