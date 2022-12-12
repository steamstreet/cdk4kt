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
        maven {
            url = uri("https://maven.pkg.jetbrains.space/steamstreet/p/vg/steamstreet")

            credentials {
                username = (project.findProperty("steamstreet.space.username") as? String) ?: "${'$'}System.env.JB_SPACE_CLIENT_ID"
                password = (project.findProperty("steamstreet.space.password") as? String) ?: "${'$'}System.env.JB_SPACE_CLIENT_SECRET"
            }
        }
    }
}