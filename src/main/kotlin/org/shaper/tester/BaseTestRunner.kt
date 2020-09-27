package org.shaper.tester

import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.*

object BaseTestRunner {
    //TODO shrink Any return type of output once we have a better idea what it looks like
    // TODO should list allow for a map or anything? - list should have it's own test-input object
    //TODO "Any output should be a results object we don't have yet
    fun <T> shapeEndpoint(
        endpoint: EndpointSpec,
        inputGenerator: (EndpointSpec) -> BaseTestInput,
        outputGenerator: (EndpointSpec, BaseTestInput, Sequence<TestResult>) -> T
    ) : T {
        //TODO - document input-output interface/how-to
        val paramValues = inputGenerator(endpoint)
        // This doesn't actually run until the out put gen because it's a sequence!
        val results = paramValues.map { runTest(it, endpoint ) }
        return outputGenerator(endpoint, paramValues, results)
    }

    fun runTest(testInput: TestInputConcretion, endpoint: EndpointSpec): TestResult {
        //TODO implement
        println("${testInput.queryParams["offset"]} || ${testInput.queryParams["limit"]}")
        return endpoint.callWithConcretion(testInput)
    }


}
