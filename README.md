# CDK4KT

Provides a set of wrappers around the CDK that leverage the 
DSL capabilities of Kotlin.

#### TypeScript

```typescript
const commandFunction = new Function(this, "SomeFunction", {
    runtime: Runtime.JAVA_11,
    handler: "com.steamstreet.example.SomeClass::execute",
    code: Code.fromAsset("src.jar"),
    timeout: Duration.minutes(5),
    memorySize: 2048
})
```

#### CDK4KT

```kotlin
val commandFunction = Function("SomeFunction") {
    runtime(Runtime.JAVA_11)
    handler("com.steamstreet.example.SomeClass::execute")
    code(Code.fromAsset("src.jar"))
    timeout(Duration.minutes(5))
    memorySize(2048)
}
```

### Using CDK4KT

For the most part, the main part of CDK4KT simply allows you to use
a dsl for the builders. It also sets the Construct as the receiver for
most calls so that it isn't required to be passed in.

Using Kotlin with the standard Java CDK, you would write:

```kotlin
Function(this, "SomeFunction", FunctionProps.builder().apply {
    // configure your function
}.build())
```

and using CDK4KT, the same function:

```kotlin
Function("SomeFunction") {
    // configure your function
}
```

The library will be enhanced with more Kotlin-like functionality, but
given the sheer size of the CDK, it is unlikely that many constructs
will be changed except those that are used by most stacks.
