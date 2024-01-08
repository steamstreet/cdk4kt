import org.gradle.api.Project.GRADLE_PROPERTIES
import java.util.*

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

Properties().let { props ->
    rootDir.toPath().resolveSibling(GRADLE_PROPERTIES).toFile().inputStream().use {
        props.load(it)
    }
    props.forEach { key, value -> project.ext.set(key.toString(), value.toString()) }
}

dependencies {
    api("software.amazon.awscdk:aws-cdk-lib:${findProperty("cdkVersion")}")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    api("com.squareup:kotlinpoet:1.6.0")
    api("org.jetbrains.kotlin:kotlin-reflect")
}
