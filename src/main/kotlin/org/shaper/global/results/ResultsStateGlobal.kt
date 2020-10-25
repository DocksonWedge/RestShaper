package org.shaper.global.results

import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.Serializable
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec

@Serializable
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

    fun save(
        endpoint: EndpointSpec,
        responseCode: Int,
        input: TestInputConcretion,
        result: TestResult
    ) {
        val previousList = index
            .getOrPut(endpoint.paramUrl(), { mutableMapOf() })
            .getOrPut(endpoint.method, { mutableMapOf() })
            .getOrPut(responseCode, { mutableMapOf() })
            .getOrPut(input.hashCode(), { mutableListOf() })
        previousList.add(result)
    }

    fun getResultsFromEndpoint(endpoint: EndpointSpec): List<TestResult> {
        return index[endpoint.paramUrl()]?.get(endpoint.method)
            ?.flatMap { inputKey -> inputKey.value.flatMap { it.value } }
            ?: throw error("No results found for specified endpoint ${endpoint.method}:${endpoint.paramUrl()}")
    }

    fun getAllResults(): List<TestResult> {
        return index.flatMap { url ->
            url.value.flatMap { method ->
                method.value.flatMap { responseCode ->
                    responseCode.value.flatMap {
                        it.value
                    }
                }
            }
        }
    }

    fun getStatusCodesFromEndpoint(endpoint: EndpointSpec): List<Int> {
        return getResultsFromEndpoint(endpoint).map { it.response.statusCode }
    }

    fun getIndexFromStatusCode(endpoint: EndpointSpec, statusCode: Int): Map<Int, MutableList<TestResult>> {
        return index[endpoint.paramUrl()]?.get(endpoint.method)?.get(statusCode) ?: mapOf()
    }

    fun loadInitialResultsSet(loadFunction: () -> Unit) {
        loadFunction()
    }

    fun clearResults() {
        index.clear()
    }
}