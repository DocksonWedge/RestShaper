package org.shaper.entry

import io.swagger.v3.oas.models.PathItem.HttpMethod.GET
import org.shaper.config.runnerConfig
import org.shaper.generators.SimpleInputGenerator

fun dataAtWorkRun(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "http://api.dataatwork.org/v1/spec/skills-api.json"
            endpoints = listOf(
                GET to "/jobs",
                GET to "/skills"
            )
        }
    }.run()
}
