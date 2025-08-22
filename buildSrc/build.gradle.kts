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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${findProperty("kotlinVersion")}")
    api("software.amazon.awscdk:aws-cdk-lib:${findProperty("cdkVersion")}")
    api("com.squareup:kotlinpoet:2.2.0")
    api("org.jetbrains.kotlin:kotlin-reflect")
}
