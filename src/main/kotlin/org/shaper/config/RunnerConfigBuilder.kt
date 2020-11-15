package org.shaper.config

import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec
import org.shaper.global.results.Results
import org.shaper.tester.BaseTestRunner

class RunnerConfigBuilder {
    var inputFunction: (EndpointSpec) -> BaseTestInput = SimpleInputGenerator(5)::getInput
    var outputFunction: (EndpointSpec, Sequence<TestResult>) -> Boolean = Results::saveToGlobal
    var summarizeFunction: (EndpointSpec) -> Unit = Results::printSummary
    lateinit var endpointConfig: EndpointConfigBuilder.() -> Unit

    fun run(maxChainDepth: Int = 1): Boolean {
        var passing = true
        val endpointList = EndpointConfigBuilder()
            .apply(endpointConfig)
            .build()
        (0..maxChainDepth).forEach { _ -> //todo multithread this
            endpointList.forEach { endpointSpec -> //todo multithread this?
                passing = BaseTestRunner.shapeEndpoint(
                    endpointSpec,
                    inputFunction,
                    outputFunction
                ) && passing
                summarizeFunction(endpointSpec)
            }
        }
        return passing
    }
}