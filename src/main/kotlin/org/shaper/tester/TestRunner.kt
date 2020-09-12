package org.shaper.tester

import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.SimpleTestInput
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.*

object TestRunner {
    //TODO shrink Any return type of output once we have a better idea what it looks like
    // TODO should list allow for a map or anything? - list should have it's own test-input object
    //TODO "Any output should be a results object we don't have yet
    fun <T> shapeEndpoint(
        endpoint: EndpointSpec,
        inputGenerator: (EndpointSpec) -> BaseTestInput,
        outputGenerator: (EndpointSpec, BaseTestInput, List<TestResult>) -> T
    ) : T {
        //TODO - document input-output interface/how-to
        val paramValues = inputGenerator(endpoint)
        //TODO - turn list of results into TestResults object?
        val results = paramValues.map { runTest(it, endpoint ) }
        return outputGenerator(endpoint, paramValues, results.toList())
    }


    fun runTest(testInput: TestInputConcretion, endpoint: EndpointSpec, iterations : Int = 50): TestResult {
        //TODO implement
        (1..iterations).forEach {

        }
        return TestResult()
    }


}
