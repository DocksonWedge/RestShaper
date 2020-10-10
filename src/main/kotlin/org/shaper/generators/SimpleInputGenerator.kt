package org.shaper.generators

import arrow.core.extensions.sequence.foldable.size
import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonObject
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec
import org.shaper.generators.model.SimpleTestInput

//simple doesn't read past results?
class SimpleInputGenerator(
    val numCases: Int = 5,
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
            endpoint.queryParams.mapValues { getParamVals(it.value, numCases) },
            endpoint.pathParams.mapValues { getParamVals(it.value, numCases) },
            endpoint.headerParams.mapValues { getParamVals(it.value, numCases) },
            endpoint.cookieParams.mapValues { getParamVals(it.value, numCases) },
            sequenceOf<JsonObject>() //TODO - implement body gen
        )
    }
    // TODO calculate function based on requirements, then pass it in
    // TODO can we have a way here to know when we have done something invalid and push that to the expected results
    private fun getParamVals(spec: ParameterSpec, numVals: Int = 5): Sequence<*> {
        return when (spec.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Long::class -> RandomLongGenerator(numVals)
            String::class -> RandomStringGenerator(numVals)
            else -> throw NotImplementedError(
                "Parameters with specs other than " +
                        "integer or string are not yet implemented."
            )
        }
    }

    class RandomLongGenerator(max: Int) :
        RandomBaseGenerator<Long>(
            max,
            { idx: Int ->
                faker.number().numberBetween(-100L, 100L)
            }
        )

    class RandomStringGenerator(max: Int) :
        RandomBaseGenerator<String>(
            max,
            { idx: Int ->
                faker.regexify("[A-z1-9]{0,10}")
            }
        )

    abstract class RandomBaseGenerator<T>(val count: Int, val fakerFun: (Int) -> T) : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            private var currentIdx = 0
            override fun next(): T {
                currentIdx++
                return fakerFun(currentIdx)
            }

            override fun hasNext(): Boolean {
                // negative number means infinite!
                return currentIdx < count || count < 0
            }
        }
    }
}


