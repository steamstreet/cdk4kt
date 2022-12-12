plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm {}

    sourceSets["jvmMain"].apply {
        kotlin.srcDir("build/cdk/wrappers")

        dependencies {
            api(libs.aws.cdk)
            api(libs.kotlin.serialization.json)
            api("org.jetbrains.kotlin:kotlin-reflect")
        }
    }
}

val wrappers = tasks.register<GenerateKotlinWrappers>("generate-wrappers") {
    outputDir.set(File(buildDir, "cdk/wrappers").canonicalPath)
    outputPackage.set("com.steamstreet.cdk.kotlin")
}
tasks["jvmMainClasses"].dependsOn(wrappers)

publishing {
    repositories {
        maven("https://steamstreet-141660060409.d.codeartifact.us-west-2.amazonaws.com/maven/steamstreet/")
    }
}