package org.shaper.global.results

import mu.KotlinLogging
import org.shaper.generators.model.TestResult
import org.shaper.global.results.ResultsStateGlobal.getResultsFromStatusCode
import org.shaper.global.results.ResultsStateGlobal.getStatusCodesFromEndpoint
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec

object Results {
    private val logger = KotlinLogging.logger {}

    // TODO purify function so it doesn't actually touch the global?
    @Synchronized
    fun saveToGlobal(
        endpoint: EndpointSpec,
        results: Sequence<TestResult>
        // TODO - Paramterize predicate to determine failing test?
    ): Boolean {
        var allPassed = true
        results.forEach { result ->
            allPassed = saveResultState(result, endpoint) && allPassed
            ResultsFieldsGlobal.save(result)
        }
        return allPassed
    }

    private fun saveResultState(result: TestResult, endpoint: EndpointSpec): Boolean {
        ResultsStateGlobal.save(
            endpoint,
            result.response.statusCode,
            result.input,
            result
        )
        return if (isFailing(endpoint, result)) {
            addParamResult(endpoint.queryParams, result.input.queryParams, addFailing)
            false
        } else {
            true
        }
    }

    //TODO get linkages function - takes one result an previous links
    fun printSummary(endpoint: EndpointSpec) {
        logger.info { "Summary for endpoint ${endpoint.method} : ${endpoint.path}" }
        getStatusCodesFromEndpoint(endpoint).forEach {
            val numResponses = getResultsFromStatusCode(endpoint, it).size
            logger.info { "  Status $it returned $numResponses times." }
        }
    }

    @Synchronized
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
    fun clearGlobals(){
        ResultsFieldsGlobal.clearResults()
        ResultsStateGlobal.clearResults()
    }
    private val addPassing = { param: ParameterSpec, value: Any ->
        //param.info.addPassingValue(value)
    }
    private val addFailing = { param: ParameterSpec, value: Any ->
        param.info.addFailingValue(value)
    }

    private fun isFailing(
        endpoint: EndpointSpec,
        result: TestResult
    ): Boolean {
        return result.response.statusCode >= 400
    }

}