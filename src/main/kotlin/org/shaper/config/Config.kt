package org.shaper.config

fun runnerConfig(lambda: RunnerConfigBuilder.() -> Unit): RunnerConfigBuilder {
    return RunnerConfigBuilder().apply(lambda)
}