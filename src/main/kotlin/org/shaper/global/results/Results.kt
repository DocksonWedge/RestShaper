package org.shaper.global.results

import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec

object Results {
    // TODO cap the max executions?
    fun saveToGlobal(
        endpoint: EndpointSpec,
        results: Sequence<TestResult>
        // TODO - Paramterize predicate to determine failing test?
    ): Boolean {
        var allPassed = true
        results.forEach { result ->
            ResultsStateGlobal.saveToGlobal(
                endpoint,
                result.response.statusCode,
                result.input,
                result
            )
            allPassed == allPassed && isFailing(endpoint, result)
        }
        return allPassed
    }

    private fun isFailing(
        endpoint: EndpointSpec,
        result: TestResult
    ): Boolean {
        return result.response.statusCode >= 500
    }

}