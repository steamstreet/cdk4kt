import org.gradle.api.Project.GRADLE_PROPERTIES
import java.util.*

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

// Load properties from gradle.properties to get versions
Properties().let { props ->
    rootDir.toPath().resolveSibling(GRADLE_PROPERTIES).toFile().inputStream().use {
        props.load(it)
    }
    props.forEach { key, value -> project.ext.set(key.toString(), value.toString()) }
}

dependencies {
    // Use versions from the version catalog (we can't directly access catalog in buildSrc)
    // So we use the same versions defined in libs.versions.toml
    api("software.amazon.awscdk:aws-cdk-lib:${findProperty("cdkVersion")}")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    api("com.squareup:kotlinpoet:1.16.0")
    api("org.jetbrains.kotlin:kotlin-reflect")
}
