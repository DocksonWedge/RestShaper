package org.shaper.global.results

import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec
import kotlin.concurrent.getOrSet

@Serializable
object ResultsStateGlobal {
    // map endpoint, method, response code, input -> return list<result>
    val index = ThreadLocal<MutableMap<String,
            MutableMap<HttpMethod,
                    MutableMap<Int,
                            MutableMap<Int,
                                    MutableList<TestResult>
                                    >
                            >
                    >
            >>()
    fun getIndex(): MutableMap<String, MutableMap<HttpMethod, MutableMap<Int, MutableMap<Int, MutableList<TestResult>>>>> {
        return index.getOrSet { mutableMapOf() }
    }

    @Synchronized
    fun save(
        endpoint: EndpointSpec,
        responseCode: Int,
        input: TestInputConcretion,
        result: TestResult
    ) {
        val previousList = index.getOrSet { mutableMapOf() }
            .getOrPut(endpoint.paramUrl(), { mutableMapOf() })
            .getOrPut(endpoint.method, { mutableMapOf() })
            .getOrPut(responseCode, { mutableMapOf() })
            .getOrPut(input.hashCode(), { mutableListOf() })
        previousList.add(result)
    }

    fun getResultsFromEndpoint(endpoint: EndpointSpec): List<TestResult> {
        return index.getOrSet { mutableMapOf() }[endpoint.paramUrl()]
            ?.get(endpoint.method)
            ?.flatMap { inputKey -> inputKey.value.flatMap { it.value } }
            ?: throw error("No results found for specified endpoint ${endpoint.method}:${endpoint.paramUrl()}")
    }

    fun getAllResults(): List<TestResult> {
        return index.getOrSet { mutableMapOf() }
            .flatMap { url ->
            url.value.flatMap { method ->
                method.value.flatMap { responseCode ->
                    responseCode.value.flatMap {
                        it.value
                    }
                }
            }
        }
    }

    fun getStatusCodesFromEndpoint(endpoint: EndpointSpec): Set<Int> {
        return getResultsFromEndpoint(endpoint).map { it.response.statusCode }.toSet()
    }

    fun getIndexFromStatusCode(endpoint: EndpointSpec, statusCode: Int): Map<Int, MutableList<TestResult>> {
        return index.getOrSet { mutableMapOf() }[endpoint.paramUrl()]
            ?.get(endpoint.method)
            ?.get(statusCode)
            ?: mapOf()
    }

    fun getResultsFromStatusCode(endpoint: EndpointSpec, statusCode: Int): List<TestResult> {
        return getIndexFromStatusCode(endpoint, statusCode).flatMap { it.value }
    }

    fun loadInitialResultsSet(loadFunction: () -> Unit) {
        loadFunction()
    }

    @Synchronized
    fun clearResults() {
        index.get()?.clear()
        index.remove()
    }
}