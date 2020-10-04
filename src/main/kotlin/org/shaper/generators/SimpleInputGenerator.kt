package org.shaper.generators

import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonObject
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec
import org.shaper.generators.model.SimpleTestInput
import org.shaper.generators.model.TestResults
import kotlin.random.Random

//simple doesn't read past results?
class SimpleInputGenerator(
    val numCases: Int = 5,
    val additionalConfig: (EndpointSpec, SimpleTestInput) -> Unit
    = { endpointSpec: EndpointSpec, testInput: SimpleTestInput -> }
) {
    companion object{
        //instantiate this as little as possible using a static companion - it is apparently very slow
        val faker = Faker()
    }
    //TODO this is the functional test starting point
    fun getInput(endpoint: EndpointSpec): SimpleTestInput {
        return SimpleTestInput(
            //TODO  - divide numCases across  param vals evenly. Somehow.
            endpoint.queryParams.mapValues { getParamVals(it.value, numCases) },
            endpoint.pathParams.mapValues { getParamVals(it.value, numCases) },
            endpoint.headerParams.mapValues { getParamVals(it.value, numCases) },
            endpoint.cookieParams.mapValues { getParamVals(it.value, numCases) },
            listOf<JsonObject>() //TODO - implement body gen
        )
    }

    //TODO - iterators for each type that can save the last position - in paramspec?
    private fun getParamVals(spec: ParameterSpec, numVals: Int = 5): List<*> {
        return when (spec.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Long::class ->
                //TODO make lazy/sequential evaluation that can take in results from previous run
                (1..numVals).map { _ ->
                    // TODO extract to configurable function that takes TestResult
                    faker.number().numberBetween(-100L, 100L)
                }
            String::class ->
                // TODO calculate function based on requirements, then pass it in
                // TODO can we have a way here to know when we have done something invalid and push that to the expected results?
                (1..numVals).map { _ ->
                    faker.regexify("[A-z1-9]{0,10}")
                }
            else -> throw NotImplementedError("Parameters with specs other than " +
                    "integer or string are not yet implemented.")
        }
    }
}