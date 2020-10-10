package org.shaper.global.results

import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestResult

object ResultsStateGlobal {
    // map endpoint, method, response code, input -> return list<result>
    val index = mutableMapOf<String,
            MutableMap<HttpMethod,
                    MutableMap<Int,
                            MutableMap<BaseTestInput,
                                    MutableList<TestResult>
                                    >
                            >
                    >
            >()

    fun saveToGlobal(
        endpoint: String,
        method: HttpMethod,
        responseCode: Int,
        input: BaseTestInput,
        result: TestResult
    ) {
        val previousList = index
            .getOrPut(endpoint, { mutableMapOf() })
            .getOrPut(method, { mutableMapOf() })
            .getOrPut(responseCode, { mutableMapOf() })
            .getOrPut(input, { mutableListOf() })
        previousList.add(result)
    }

    fun loadInitialResultsSet(loadFunction: () -> Unit) {
        loadFunction()
    }

    private fun <K, V> MutableMap<K, V>.addWithReturn(key: K, item: V): V {
        this[key] = item
        return item
    }

}