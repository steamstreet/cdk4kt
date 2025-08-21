# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CDK4KT is a Kotlin DSL wrapper library for AWS CDK that provides more idiomatic Kotlin syntax for CloudFormation infrastructure-as-code. It generates Kotlin wrapper functions around AWS CDK Java classes to enable cleaner, more concise infrastructure definitions.

## Build System

### Gradle Setup
- **Kotlin Version**: 1.9.22
- **CDK Version**: 2.170.0
- **Java Toolchain**: JDK 11
- **Build Tool**: Gradle with Kotlin DSL

### Common Commands

```bash
# Build the entire project
./gradlew build

# Clean build artifacts
./gradlew clean

# Generate Kotlin CDK wrappers (runs automatically during build)
./gradlew :kotlin-cdk-lib:generate-wrappers

# Build and install to local Maven repository
./gradlew publishToMavenLocal

# Run tests
./gradlew test

# Generate documentation
./gradlew dokkaHtml

# Publish to Maven Central (requires signing properties)
./gradlew publish
```

## Architecture

### Code Generation System

The project uses a custom code generation system to create Kotlin DSL wrappers:

1. **`buildSrc/src/main/kotlin/KCDKBuilder.kt`**: Core code generator that:
   - Scans AWS CDK JAR files for classes
   - Generates Kotlin extension functions for CDK constructs
   - Creates builder pattern DSL functions for cleaner syntax
   - Groups generated code by AWS service (e.g., lambda, cloudwatch, events)

2. **`buildSrc/src/main/kotlin/wrapper-builder.kt`**: Gradle task implementation (`GenerateKotlinWrappers`) that:
   - Locates the AWS CDK JAR on the classpath
   - Invokes the KCDKBuilder to generate wrapper code
   - Outputs generated code to `build/cdk/wrappers` directory

3. **Generated Code Location**: `kotlin-cdk-lib/build/cdk/wrappers/com/steamstreet/cdk/kotlin/`
   - One file per AWS service group (e.g., `lambda.kt`, `cloudwatch.kt`)
   - Files are auto-generated and should not be edited manually

### Module Structure

- **`buildSrc/`**: Gradle build configuration and code generation logic
- **`kotlin-cdk-lib/`**: Main library module containing:
  - Manual extensions in `src/main/kotlin/com/steamstreet/cdk/kotlin/`
  - Generated wrappers in `build/cdk/wrappers/` (created during build)

### Key Design Patterns

1. **DSL Builder Pattern**: Transforms Java-style builders into Kotlin lambdas
   ```kotlin
   // Instead of: Function.Builder.create(this, "MyFunction").runtime(Runtime.JAVA_11).build()
   // Use: Function("MyFunction") { runtime(Runtime.JAVA_11) }
   ```

2. **Receiver Context**: Uses Kotlin's receiver types to eliminate passing `this` construct
   ```kotlin
   // Extension functions use the Construct as receiver
   stack.Function("MyFunction") { /* config */ }
   ```

3. **Service Grouping**: Generated code is organized by AWS service to avoid massive single files

## Development Workflow

### Making Changes to Manual Extensions

1. Edit files in `kotlin-cdk-lib/src/main/kotlin/com/steamstreet/cdk/kotlin/`
2. Key extension files:
   - `app.kt`: Entry point DSL functions for CDK apps
   - `extensions/Lambda.kt`: Lambda-specific convenience functions

### Updating CDK Version

1. Modify `cdkVersion` in `gradle.properties`
2. Run `./gradlew clean build` to regenerate wrappers for new CDK version
3. Test compatibility with existing code

### Publishing New Versions

1. Ensure signing properties are configured:
   - `signing.keyId`
   - `signing.password`
   - `signing.secretKeyRingFile`
2. Update version via `RELEASE_NAME` property or it will use CDK version with `-SNAPSHOT`
3. Run `./gradlew publish` to deploy to Maven Central

## Code Generation Details

The `KCDKBuilder` class performs sophisticated reflection-based analysis:

1. **Constructor Analysis**: Identifies CDK construct patterns (Construct, id, props)
2. **Builder Detection**: Finds classes with `builder()` methods to generate DSL functions
3. **Deprecation Handling**: Skips deprecated classes and methods
4. **Type Safety**: Preserves type information in generated Kotlin code
5. **Lambda Receivers**: Generates functions with builder lambdas as receivers

### Generated Function Types

1. **Construct Creation**: `Construct.ConstructName(id: String) { /* props */ }`
2. **Builder Shortcuts**: Functions that wrap single-parameter methods with builders
3. **Standalone Builders**: `ClassName { /* props */ }` for classes with static builders

## Testing Considerations

- Generated code is not directly tested but validated through compilation
- Manual extensions should have unit tests
- Integration testing should verify generated DSL works with actual CDK synthesis

## Common Issues and Solutions

1. **Missing Generated Code**: Run `./gradlew :kotlin-cdk-lib:generate-wrappers` explicitly
2. **Classpath Issues**: Ensure AWS CDK JAR is properly resolved in dependencies
3. **Version Conflicts**: Keep Kotlin and CDK versions aligned with AWS CDK Java requirements