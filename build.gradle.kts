buildscript {
    dependencies {
        classpath("software.amazon.awssdk:sso:2.17.295")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

val MAJOR_VERSION = 2
val MINOR_VERSION = 53

allprojects {
    group = "com.steamstreet"
    version = "$MAJOR_VERSION.$MINOR_VERSION${this.findProperty("BUILD_NUMBER")?.let { ".$it" } ?: ".0-SNAPSHOT"}"

    repositories {
        mavenCentral()
    }
}


plugins {
    kotlin("jvm") version "1.7.22" apply false
    kotlin("plugin.serialization") version "1.7.22" apply false
    kotlin("multiplatform") version "1.7.22" apply false
}


