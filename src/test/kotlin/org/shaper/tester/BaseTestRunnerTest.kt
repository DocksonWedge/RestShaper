package org.shaper.tester

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestResult
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.model.EndpointSpec

class BaseTestRunnerTest {

    @TestFactory
    fun `Test lazy evaluation of input and input happy path`() = listOf(
        "GET:/jobs" to 25, //2 params 5*5
    )
        .map { (endpoint, expected) ->
            DynamicTest.dynamicTest(
                "Verify shapeEndpoint can get all required permutations lazily"
            ) {
                val endpoints =
                    SpecFinder("http://api.dataatwork.org/v1/spec/skills-api.json", listOf(endpoint))
                        .getRelevantSpecs()

                BaseTestRunner.shapeEndpoint(
                    endpoints[0],
                    SimpleInputGenerator()::getInput
                ) { endpoint: EndpointSpec, input: BaseTestInput, results: Sequence<TestResult> ->
                    val iter = results.iterator()
                    Assertions.assertEquals(expected, results.toList().size)
                }

            }
        }
}