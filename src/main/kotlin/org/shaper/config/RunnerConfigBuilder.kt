package org.shaper.config

import mu.KotlinLogging
import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.StaticParams
import org.shaper.generators.model.TestResult
import org.shaper.global.kafka.ResultsProducer
import org.shaper.swagger.model.EndpointSpec
import org.shaper.global.results.Results
import org.shaper.global.results.ResultsFieldsGlobal
import org.shaper.tester.BaseTestRunner
import java.util.*

class RunnerConfigBuilder {
    var inputFunction: (EndpointSpec, StaticParams) -> BaseTestInput = SimpleInputGenerator(5)::getInput
    var staticParams = StaticParams()
    var outputFunction: (EndpointSpec, Sequence<TestResult>) -> Boolean = Results::saveToGlobal
    var summarizeFunction: (EndpointSpec) -> Unit = Results::printSummary
    lateinit var endpointConfig: EndpointConfigBuilder.() -> Unit

    fun run(maxChainDepth: Int = 1, runId: String = ""): Boolean {
        val runUuid = if (runId.isBlank()) UUID.randomUUID().toString() else runId
        var passing = true
        val endpointList = EndpointConfigBuilder()
            .apply(endpointConfig)
            .build()
        (1..maxChainDepth).forEach { _ -> //todo multithread this
            endpointList.forEach { endpointSpec -> //todo multithread this?
                passing = BaseTestRunner.shapeEndpoint(
                    endpointSpec,
                    staticParams,
                    inputFunction,
                    runId,
                    outputFunction
                ) && passing
                ResultsProducer.produceRunCompleteMessage(runUuid)
                summarizeFunction(endpointSpec)
            }
        }

        Results.clearGlobals()
        return passing
    }
}