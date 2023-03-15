plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    api("software.amazon.awscdk:aws-cdk-lib:2.69.+")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    api("com.squareup:kotlinpoet:1.6.0")
    api("org.jetbrains.kotlin:kotlin-reflect")
}
