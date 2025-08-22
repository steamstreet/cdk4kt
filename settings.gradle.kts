@file:Suppress("UnstableApiUsage")

rootProject.name = "kotlin-cdk"

include(":kotlin-cdk-lib")

pluginManagement {
    val kotlinVersion: String by settings
   resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace != null && requested.id.namespace!!.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    pluginManagement {
        repositories {
            gradlePluginPortal()
            mavenCentral()
        }
    }
}