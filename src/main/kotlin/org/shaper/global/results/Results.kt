package org.shaper.global.results

import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec

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
            if (isFailing(endpoint, result)) {
                allPassed = false
                addParamResult(endpoint.queryParams, result.input.queryParams, addFailing)
            } else {
                addParamResult(endpoint.queryParams, result.input.queryParams, addPassing)
            }
        }
        return allPassed
    }

    private fun addParamResult(
        endpointParams: Map<String, ParameterSpec>,
        resultParams: Map<String, *>,
        addFun: (ParameterSpec, Any) -> Unit
    ) {
        endpointParams.forEach {
            addFun(
                it.value, resultParams[it.key]
                    ?: throw error(
                        "Mismatched parameter in results saving!"
                    )
            )
        }
    }

    private val addPassing = { param: ParameterSpec, value: Any ->
        param.addPassingValue(value)
    }
    private val addFailing = { param: ParameterSpec, value: Any ->
        param.addFailingValue(value)
    }

    private fun isFailing(
        endpoint: EndpointSpec,
        result: TestResult
    ): Boolean {
        return result.response.statusCode >= 500
    }

}