package org.shaper.global.results

import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec

object ResultsStateGlobal {
    // map endpoint, method, response code, input -> return list<result>
    val index = mutableMapOf<String,
            MutableMap<HttpMethod,
                    MutableMap<Int,
                            MutableMap<Int,
                                    MutableList<TestResult>
                                    >
                            >
                    >
            >()

    fun saveToGlobal(
        endpoint: EndpointSpec,
        input: TestInputConcretion,
        responseCode: Int,
        result: TestResult
    ) {
        val previousList = index
            .getOrPut(endpoint.paramUrl(), { mutableMapOf() })
            .getOrPut(endpoint.method, { mutableMapOf() })
            .getOrPut(input.hashCode(),  { mutableMapOf() })
            .getOrPut(responseCode,{ mutableListOf() })
        previousList.add(result)
    }
    fun getResultsFromEndpoint(endpoint: EndpointSpec) :List<TestResult> {
        return index[endpoint.paramUrl()]?.get(endpoint.method)?.flatMap { inputKey -> inputKey.value.flatMap { it.value } }
            ?: throw error("No results found for specified endpoint ${endpoint.method}:${endpoint.paramUrl()}")
    }
    fun loadInitialResultsSet(loadFunction: () -> Unit) {
        loadFunction()
    }
}