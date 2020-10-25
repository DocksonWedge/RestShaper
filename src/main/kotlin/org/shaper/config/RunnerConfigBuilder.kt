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
    lateinit var endpointConfig: EndpointConfigBuilder.() -> Unit

    fun run(): Boolean {
        var passing = true
         EndpointConfigBuilder()
            .apply(endpointConfig)
            .build()
            .forEach { endpointSpec ->
                passing = BaseTestRunner.shapeEndpoint(
                    endpointSpec,
                    inputFunction,
                    outputFunction
                ) && passing
            }
        return passing
    }
}