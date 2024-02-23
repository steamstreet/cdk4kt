package com.steamstreet.cdk.kotlin

import software.amazon.awscdk.App
import software.amazon.awscdk.Stack

public fun app(block: App.() -> Unit) {
    App().apply(block).synth()
}

@Suppress("unused")
public fun stack(id: String, name: String, init: Stack.() -> Unit) {
    app {
        stack(id, name, init)
    }
}