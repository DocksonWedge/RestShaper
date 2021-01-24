package org.shaper.config

fun runnerConfig(lambda: RunnerConfigBuilder.() -> Unit): RunnerConfigBuilder {
    return RunnerConfigBuilder().apply(lambda)
}

fun endpointConfig(lambda: EndpointConfigBuilder.() -> Unit): EndpointConfigBuilder {
    return EndpointConfigBuilder().apply(lambda)
}