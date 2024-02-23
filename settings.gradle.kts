@file:Suppress("UnstableApiUsage")

rootProject.name = "kotlin-cdk"

include(":kotlin-cdk-lib")

pluginManagement {
    val kotlinVersion : String by settings

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace != null && requested.id.namespace!!.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}

dependencyResolutionManagement {
    val cdkVersion : String by settings

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

    versionCatalogs {
        create("libs") {
            val kotlinSerializationVersion = version("kotlin-serialization", "1.4.1")
            library("kotlin-serialization-core", "org.jetbrains.kotlinx",
                "kotlinx-serialization-core").versionRef(
                kotlinSerializationVersion
            )
            library("kotlin-serialization-json", "org.jetbrains.kotlinx",
                "kotlinx-serialization-json").versionRef(
                kotlinSerializationVersion
            )

            library("aws-cdk", "software.amazon.awscdk", "aws-cdk-lib").version(cdkVersion)
            library("aws-cdk-apigateway", "software.amazon.awscdk", "apigatewayv2-integrations-alpha").version("${cdkVersion}-alpha.0")

            library("kotlin-poet", "com.squareup", "kotlinpoet").version("1.6.0")
            library("kotlin-date-time", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.0")

            val JACKSON_VERSION = "2.16.1"
            library("jackson", "com.fasterxml.jackson.module", "jackson-module-kotlin")
                .version(JACKSON_VERSION)
            library("jackson-jdk8", "com.fasterxml.jackson.datatype", "jackson-datatype-jdk8")
                .version(JACKSON_VERSION)
            library("jackson-jsr310", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310")
                .version(JACKSON_VERSION)
            library(
                "jackson-dataformat-yaml", "com.fasterxml.jackson.dataformat",
                "jackson-dataformat-yaml"
            ).version(JACKSON_VERSION)

        }
    }
}