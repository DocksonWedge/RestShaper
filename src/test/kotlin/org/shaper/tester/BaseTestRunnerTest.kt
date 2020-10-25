package org.shaper.tester

import io.mockk.every
import io.mockk.mockk
import io.restassured.http.Headers
import io.restassured.response.Response
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.global.results.Results
import org.shaper.global.results.ResultsStateGlobal
import org.shaper.global.results.ResultsStateGlobal.getResultsFromEndpoint
import org.shaper.global.results.ResultsStateGlobal.getStatusCodesFromEndpoint
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.model.EndpointSpec


class BaseTestRunnerTest {
    @ResourceLock("ResultsStateGlobal")
    @TestFactory
    fun `Test lazy evaluation of input and input happy path`() = listOf(
        "GET:/jobs" to 25, //2 params 5*5
    ).map { (endpoint, expected) ->
        DynamicTest.dynamicTest("Verify shapeEndpoint can get all required permutations lazily")
        {
            ResultsStateGlobal.clearResults()
            val endpoints =
                SpecFinder("http://api.dataatwork.org/v1/spec/skills-api.json", listOf(endpoint))
                    .getRelevantSpecs()
            //mock out call to external system for speed
            val mockResponse = mockk<Response>()
            every { mockResponse.statusCode } returns 200
            every { mockResponse.asString() } returns "{}"
            every { mockResponse.headers } returns Headers()
            every { mockResponse.cookies } returns mapOf()
            endpoints[0].callFunction = { e: EndpointSpec, i: TestInputConcretion ->
                TestResult(mockResponse, i, e.endpoint)
            }

            val passed = BaseTestRunner.shapeEndpoint(
                endpoints[0],
                SimpleInputGenerator()::getInput
            ) { endpoint: EndpointSpec, results: Sequence<TestResult> ->
                val resultsList = results.toList()
                Assertions.assertEquals(expected, resultsList.size)
                Results.saveToGlobal(endpoint, resultsList.asSequence())
            }

            Assertions.assertEquals(true, passed)
            //todo rethink how sequences loop over all the things
            val allResultsFromGlobal = getResultsFromEndpoint(endpoints[0])
            Assertions.assertEquals(expected, allResultsFromGlobal.size)
            Assertions.assertDoesNotThrow {
                getStatusCodesFromEndpoint(endpoints[0]).forEach { it < 1000 }
            }
            Assertions.assertTrue(endpoints[0].params.values.flatMap { it.passingValues }.size > 3)
        }
    }
}