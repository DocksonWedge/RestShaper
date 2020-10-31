package org.shaper.generators

import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonObject
import org.shaper.swagger.model.EndpointSpec
import org.shaper.generators.model.SimpleTestInput
import org.shaper.global.results.ResultsStateGlobal
import org.shaper.swagger.model.ParamInfo
import java.util.concurrent.ThreadLocalRandom

//simple doesn't read past results?
class SimpleInputGenerator(
    val numCases: Int = 25,
    val additionalConfig: (EndpointSpec, SimpleTestInput) -> Unit
    = { endpointSpec: EndpointSpec, testInput: SimpleTestInput -> }
) {
    companion object {
        //instantiate this as little as possible using a static companion - it is apparently very slow
        val faker = Faker()
        val threadLocalRandom = ThreadLocalRandom.current()
    }

    //TODO this is the functional test starting point
    fun getInput(endpoint: EndpointSpec): SimpleTestInput {
        return SimpleTestInput(
            //TODO  - divide numCases across  param vals evenly. Somehow.
            endpoint.queryParams.mapValues { getParamVals(it.value.info) },
            endpoint.pathParams.mapValues { getParamVals(it.value.info) },
            endpoint.headerParams.mapValues { getParamVals(it.value.info) },
            endpoint.cookieParams.mapValues { getParamVals(it.value.info) },
            sequenceOf<JsonObject>(), //TODO - implement body gen
            numCases
        )
    }

    // TODO calculate function based on requirements, then pass it in
    // TODO can we have a way here to know when we have done something invalid and push that to the expected results
    private fun getParamVals(param: ParamInfo<Any>): Sequence<*> {
        return when (param.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Long::class -> RandomLongGenerator(param)
            String::class -> RandomStringGenerator(param)
            Double::class -> RandomDoubleGenerator(param)
            else -> throw NotImplementedError(
                "Parameters with specs other than " +
                        "integer, number, or string are not yet implemented."
            )
        }
    }

    class RandomLongGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<Long>(
            param,
            { 0L },
            { faker.number().randomNumber() },
            { p -> faker.number().numberBetween(p.minInt, p.maxInt) }
    )

    class RandomDoubleGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<Double>(
            param,
            { 0.0 },
            { faker.number().randomDouble(5, -10000000,10000000) },
            { p -> threadLocalRandom.nextDouble(p.minDecimal, p.maxDecimal) }
        )

    class RandomStringGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<String>(
            param,
            { "" },
            { faker.regexify("[A-z1-9]{0,10}") },
            { p -> p.passingValues.random().toString() }
        )

    abstract class RandomBaseGenerator<T>(
        val param: ParamInfo<Any>,
        val nullFun: (ParamInfo<Any>) -> T,
        val invalidFun: (ParamInfo<Any>) -> T,
        val validFun: (ParamInfo<Any>) -> T,
    ) : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {

            override fun next(): T {
                val percentNull = .25
                val percentInvalid = .15
                val randomNum = faker.number().randomDouble(3, 0, 1)
                return if (randomNum < percentNull) {
                    nullFun(param)
                } else if (randomNum < percentNull + percentInvalid) {
                    invalidFun(param)
                } else {
                    validFun(param)
                }
            }

            override fun hasNext(): Boolean {
                return true
            }
        }

        fun getErroredParams(endpoint: EndpointSpec) {
            ResultsStateGlobal.getIndexFromStatusCode(endpoint, 500)
        }
    }
}


