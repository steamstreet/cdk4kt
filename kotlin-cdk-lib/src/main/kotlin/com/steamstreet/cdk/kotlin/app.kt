package com.steamstreet.cdk.kotlin

import software.amazon.awscdk.App
import software.amazon.awscdk.Stack

fun app(block: App.() -> Unit) {
    App().apply(block).synth()
}

@Suppress("unused")
fun stack(id: String, name: String, init: Stack.() -> Unit) {
    app {
        stack(id, name, init)
    }
}