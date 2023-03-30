import java.net.URI

plugins {
    kotlin("multiplatform")
    id("maven-publish")
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
tasks["compileKotlinJvm"].dependsOn(wrappers)

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/steamstreet/cdk4kt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}