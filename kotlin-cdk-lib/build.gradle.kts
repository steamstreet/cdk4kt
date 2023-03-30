plugins {
    kotlin("multiplatform")
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