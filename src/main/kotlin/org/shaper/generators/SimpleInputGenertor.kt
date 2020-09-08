package org.shaper.generators

import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonObject
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec
import org.shaper.generators.model.TestInput

//simple doesn't read past results?
class SimpleInputGenertor(
    numCases: Int = 50,
    additionalConfig: (EndpointSpec, TestInput) -> Unit
    = { endpointSpec: EndpointSpec, testInput: TestInput -> }
) {
    lateinit var whereStopped: () -> Unit
    fun getInput(endpoint: EndpointSpec): TestInput {
        return TestInput(
            endpoint.queryParams.mapValues { getParamVals(it.value) },
            endpoint.pathParams.mapValues { getParamVals(it.value) },
            endpoint.headerParams.mapValues { getParamVals(it.value) },
            endpoint.cookieParams.mapValues { getParamVals(it.value) },
            listOf<JsonObject>() //TODO - implement body gen
        )
    }

    //TODO - iterators for each type that can save the last position - in paramspec?
    private fun getParamVals(spec: ParameterSpec, numVals: Int = 5): List<*> {
        return when (spec.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Int::class -> (1..numVals).toList()
                .map {
                    Faker()
                        .number()
                        .numberBetween(-100, 100)
                }
            else -> throw NotImplementedError("Parameters with specs other than integer are not yet implemented.")
        }
    }
}