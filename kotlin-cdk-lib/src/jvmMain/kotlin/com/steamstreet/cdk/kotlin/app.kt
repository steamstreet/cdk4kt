package com.steamstreet.cdk.kotlin

import software.amazon.awscdk.App
import software.amazon.awscdk.Stack

fun app(block: App.() -> Unit) {
    App().apply(block).synth()
}

fun stack(id: String, name: String, init: Stack.() -> Unit): Unit {
    app {
        stack(id, name, init)
    }
}