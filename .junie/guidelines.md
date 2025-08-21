Project: CDK4KT (Kotlin DSL wrappers for AWS CDK)

This document captures project-specific details useful for advanced contributors: how the build works, how code is generated, how to run and add tests, and gotchas observed in this repository.

Build and Configuration
- Gradle and Toolchains
  - Use the included Gradle Wrapper: ./gradlew ...
  - Java toolchain: The kotlin-cdk-lib module is pinned to Java 11 (java.toolchain.languageVersion = 11).
  - The settings script applies org.gradle.toolchains.foojay-resolver-convention (1.0.0) which can auto-provision JDKs; ensure network access for toolchain resolution.
- Versions and Properties
  - gradle.properties (in repo) defines:
    - org.gradle.jvmargs=-Xmx4096m (4G heap for Gradle)
    - kotlinVersion=2.1.20 (Kotlin plugin version is propagated via settings.gradle.kts)
    - cdkVersion=2.170.0 (used for AWS CDK dependencies and version catalog)
  - Root build.gradle.kts sets group and version across all projects. If RELEASE_NAME is provided, version is RELEASE_NAME without leading v; otherwise defaults to "$cdkVersion-SNAPSHOT".
- Publishing/Signing
  - Root build applies io.github.gradle-nexus.publish-plugin for Sonatype publishing; credentials expected as Gradle properties sonatypeUsername/sonatypePassword.
  - kotlin-cdk-lib applies signing. Signing is conditionally enabled: tasks withType<Sign> only run if signing.keyId is set. This enables local/dev builds without signing keys.
  - kotlin-cdk-lib produces javadocJar using Dokka (org.jetbrains.dokka 1.9.10) and attaches it to the Maven publication.
- Dependency Resolution and Version Catalog
  - settings.gradle.kts defines a libs version catalog. Key entries:
    - libs.aws.cdk -> software.amazon.awscdk:aws-cdk-lib:${cdkVersion}
    - libs.kotlin.serialization.json (1.4.1 in catalog; note buildSrc pins 1.6.3 — see below)
    - Jackson modules pinned to 2.16.1
  - Repositories: mavenCentral() and gradlePluginPortal().

Code Generation Workflow
- Generated Kotlin wrappers for AWS CDK live under build/cdk/wrappers and are added as an additional source directory for the main source set (java.sourceSets["main"].java.srcDir("build/cdk/wrappers")).
- The GenerateKotlinWrappers Gradle task (defined in buildSrc) runs before compileKotlin:
  - tasks.named("compileKotlin").dependsOn("generate-wrappers")
  - The task discovers the aws-cdk-lib JAR on the runtimeClasspath, opens it, and uses reflection plus KotlinPoet to generate wrapper builders into the output package com.steamstreet.cdk.kotlin.
  - If aws-cdk-lib isn’t on the classpath, the task fails with "Can't find CDK lib on classpath". Ensure dependencies resolve (internet access to Maven Central) before compileKotlin.
- buildSrc loads properties upstream of the repo root
  - buildSrc/build.gradle.kts loads properties from rootDir.resolveSibling("gradle.properties"). This means any gradle.properties located one directory above the repository root will be merged into project properties. Be aware of this when running in environments that may have a parent gradle.properties (CI/CD, developer machines). This is intentional in this repo and can affect versions/credentials.
- Example build commands
  - Build only the library: ./gradlew :kotlin-cdk-lib:build
  - Generate wrappers explicitly (for debugging): ./gradlew :kotlin-cdk-lib:generate-wrappers --info
  - Assemble and publish (requires proper credentials): ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

Testing
- Framework
  - The kotlin-cdk-lib module is configured for Kotlin/JUnit 5 tests:
    - dependencies { testImplementation(kotlin("test")); testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2") }
    - tasks.test { useJUnitPlatform() }
- Running tests
  - Run all tests in the module: ./gradlew :kotlin-cdk-lib:test
  - Run with more logging: ./gradlew :kotlin-cdk-lib:test --info --stacktrace
- Adding tests
  - Place tests under kotlin-cdk-lib/src/test/kotlin.
  - Prefer kotlin.test assertions for portability; they map to JUnit 5 via the kotlin-test-junit5 bindings.
  - Example test (validated locally before writing this guideline):
    - Subject: the String.toSlug() extension in com.steamstreet.cdk.kotlin.extensions.Lambda.kt.
      It normalizes strings by replacing whitespace with '-', strips non-word/non-hyphen characters, lowercases, and does not collapse repeated dashes.
    - Sample test code:
      package com.steamstreet.cdk.kotlin.extensions

      import kotlin.test.Test
      import kotlin.test.assertEquals

      class ToSlugTest {
          @Test
          fun toSlug_examples() {
              assertEquals("hello-world", "Hello World".toSlug())
              assertEquals("a--b---c", "A  B   C".toSlug())
              assertEquals("scheduled-cron0-5---", "Scheduled: Cron(0 5 * * ?)".toSlug())
              assertEquals("emoji-", "Emoji 😊".toSlug())
              assertEquals("kotlin-cdk", "Kotlin-CDK".toSlug())
          }
      }
    - All of the above assertions passed in our validation run.
- Notes when testing code generation
  - Most of the library provides extension DSLs and generated wrappers; unit tests should aim at pure helpers/transformations (e.g., string utilities) or small wrapper behaviors that don’t require AWS context.
  - For constructs requiring AWS CDK types, prefer validating parameters/DSL building logic and avoid synthesizing whole apps in unit tests. If necessary, use software.amazon.awscdk.App() and Stack in isolated tests, but be mindful of runtime and complexity.

Additional Development Notes
- Kotlin explicit API
  - kotlin { explicitApi = Warning } is enabled in kotlin-cdk-lib. Public APIs must include explicit visibility and types. CI/local builds will warn on implicit public APIs.
- Dokka documentation
  - Generate HTML docs: ./gradlew :kotlin-cdk-lib:dokkaHtml
  - A javadocJar is built from Dokka output for publishing.
- Code style and structure
  - Packages: public DSL surface is under com.steamstreet.cdk.kotlin. The extensions live under subpackages like com.steamstreet.cdk.kotlin.extensions.
  - Do not edit generated sources under build/cdk/wrappers; regenerate via the task. Any manual adjustments belong in hand-written extension files.
- Version alignment
  - There is a minor divergence between version catalogs and buildSrc regarding kotlinx-serialization (catalog 1.4.1, buildSrc 1.6.3). When updating CDK or Kotlin, verify generator compatibility and consider aligning these versions to avoid classpath surprises inside buildSrc.
- Performance and memory
  - Wrapper generation scans the aws-cdk-lib JAR; ensure adequate heap (the repo sets -Xmx4096m) and use --info for progress. For very large CDK updates, you may need to increase heap.

Quick Start
1) Ensure Java 11 is available (the toolchain can auto-provision).
2) Build the module: ./gradlew :kotlin-cdk-lib:build
3) Run tests: ./gradlew :kotlin-cdk-lib:test
4) Generate docs (optional): ./gradlew :kotlin-cdk-lib:dokkaHtml
5) Publishing (requires credentials): ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

Cleanup Notes for This Guideline
- A temporary unit test was created and executed to validate the testing flow; it has been removed to keep the repository unchanged except for this guideline and the minimal test configuration added to kotlin-cdk-lib/build.gradle.kts. The documented sample test has been verified to pass.
