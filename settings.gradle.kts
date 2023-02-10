@file:Suppress("UnstableApiUsage")

rootProject.name = "kotlin-cdk"

include(":kotlin-cdk-lib")

dependencyResolutionManagement {
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

            library("aws-cdk", "software.amazon.awscdk", "aws-cdk-lib").version("2.64.0")
            library("aws-cdk-apigateway", "software.amazon.awscdk", "apigatewayv2-integrations-alpha").version("2.64.0")

            library("kotlin-poet", "com.squareup", "kotlinpoet").version("1.6.0")
            library("kotlin-date-time", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.0")
        }
    }
}