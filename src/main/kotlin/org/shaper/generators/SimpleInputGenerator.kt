package org.shaper.generators

import arrow.core.extensions.sequence.foldable.size
import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonObject
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec
import org.shaper.generators.model.SimpleTestInput
import org.shaper.global.results.ResultsStateGlobal

//simple doesn't read past results?
class SimpleInputGenerator(
    val numCases: Int = 25,
    val additionalConfig: (EndpointSpec, SimpleTestInput) -> Unit
    = { endpointSpec: EndpointSpec, testInput: SimpleTestInput -> }
) {
    companion object {
        //instantiate this as little as possible using a static companion - it is apparently very slow
        val faker = Faker()
    }

    //TODO this is the functional test starting point
    fun getInput(endpoint: EndpointSpec): SimpleTestInput {
        return SimpleTestInput(
            //TODO  - divide numCases across  param vals evenly. Somehow.
            endpoint.queryParams.mapValues { getParamVals(it.value) },
            endpoint.pathParams.mapValues { getParamVals(it.value) },
            endpoint.headerParams.mapValues { getParamVals(it.value) },
            endpoint.cookieParams.mapValues { getParamVals(it.value) },
            sequenceOf<JsonObject>(), //TODO - implement body gen
            numCases
        )
    }

    // TODO calculate function based on requirements, then pass it in
    // TODO can we have a way here to know when we have done something invalid and push that to the expected results
    private fun getParamVals(param: ParameterSpec): Sequence<*> {
        return when (param.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Long::class -> RandomLongGenerator(param)
            String::class -> RandomStringGenerator(param)
            else -> throw NotImplementedError(
                "Parameters with specs other than " +
                        "integer or string are not yet implemented."
            )
        }
    }

    class RandomLongGenerator(param: ParameterSpec) :
        RandomBaseGenerator<Long>(
            {
                val percentZero = .25
                if (faker.number().randomDouble(3, 0, 1) < percentZero) {
                    0
                } else {
                    faker.number().numberBetween(param.minNum, param.maxNum)
                }
            }
        )

    class RandomStringGenerator(param: ParameterSpec) :
        RandomBaseGenerator<String>(
            {
                val percentEmptyString = .25
                if (faker.number().randomDouble(3, 0, 1) < percentEmptyString) {
                    ""
                } else {
                    faker.regexify("[A-z1-9]{0,10}")
                }
            }
        )


    abstract class RandomBaseGenerator<T>(val fakerFun: () -> T) : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun next(): T {
                return fakerFun()
            }

            override fun hasNext(): Boolean {
                return true
            }
        }
        fun getErroredParams(endpoint: EndpointSpec){
            ResultsStateGlobal.getIndexFromStatusCode(endpoint, 500)
        }
    }
}


