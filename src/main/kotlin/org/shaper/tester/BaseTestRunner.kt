package org.shaper.tester

import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.StaticParams
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.*

object BaseTestRunner {
    //TODO shrink Any return type of output once we have a better idea what it looks like
    // TODO should list allow for a map or anything? - list should have it's own test-input object
    //TODO "Any output should be a results object we don't have yet
    inline fun <T> shapeEndpoint(
        endpoint: EndpointSpec,
        staticParams: StaticParams,
        inputGenerator: (EndpointSpec, StaticParams) -> BaseTestInput,
        outputGenerator: (EndpointSpec, Sequence<TestResult>) -> T
    ): T {
        //TODO - document input-output interface/how-to
        val paramValues = inputGenerator(endpoint, staticParams)
        // This doesn't actually run until the out put gen because it's a sequence!
        val results = paramValues.map { runTest(it, endpoint) }
        return outputGenerator(endpoint, results)
    }

    /**
    Use shapeEndpoint instead unless you really know what you are doing!
     */
    fun runTest(testInput: TestInputConcretion, endpoint: EndpointSpec): TestResult {
        return endpoint.callWithConcretion(testInput)
    }


}
