package org.shaper.generators

import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.shaper.swagger.model.EndpointSpec
import org.shaper.generators.model.SimpleTestInput
import org.shaper.global.results.ResultsStateGlobal
import org.shaper.swagger.model.ParamInfo
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass

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

        //TODO map any could be json elements
        fun streamMapParamSequence(param: ParamInfo<Any>): Map<String, Any> {
            //TODO add required fields handling
            return param
                .nestedParams
                .mapValues {
                    pokoToJsonElement(
                        SimpleInputGenerator()
                            .getParamVals(it.value as ParamInfo<Any>)
                            .iterator()
                            .next() ?: ""
                    )
                }
        }

        fun streamListParamSequence(param: ParamInfo<Any>): List<Any> {
            //TODO add max length handling
            return (1..faker.number().numberBetween(1, 5)).map { _ ->
                SimpleInputGenerator()
                    .getParamVals(param.listParam)
                    .iterator()
                    .next() ?: ""
            }
        }

        // TODO move converison to request body part
        private fun pokoToJsonElement(poko: Any): JsonElement {
            return if (poko is Map<*, *>) {
                JsonObject(
                    poko.mapValues { pokoToJsonElement(it.value ?: "") }
                        .mapKeys { it.key as String }
                )
            } else if (poko is List<*>) {
                // !! because w/ mapNotNull list entry should never be null
                JsonArray(poko.mapNotNull { pokoToJsonElement(it!!) })
            } else if (poko is Boolean) {
                JsonPrimitive(poko)
            } else if (poko is Number) {
                JsonPrimitive(poko)
            } else if (poko is String) {
                JsonPrimitive(poko)
            } else if (poko is JsonElement){
                poko
            } else {
                throw NotImplementedError(
                    "When converting to JSON, " +
                            "found a data type that we could not parse: $poko of type ${poko::class}"
                )
            }
        }
    }

    fun getInput(endpoint: EndpointSpec): SimpleTestInput {
        return SimpleTestInput(
            endpoint.queryParams.mapValues { getParamVals(it.value.info) },
            endpoint.pathParams.mapValues { getParamVals(it.value.info) },
            endpoint.headerParams.mapValues { getParamVals(it.value.info) },
            endpoint.cookieParams.mapValues { getParamVals(it.value.info) },
            getParamVals(endpoint.body.bodyInfo), //TODO - implement body gen
            numCases
        )
    }

    // TODO calculate function based on requirements, then pass it in
    // TODO can we have a way here to know when we have done something invalid and push that to the expected results
    fun getParamVals(param: ParamInfo<Any>?): Sequence<*> {
        if (param == null) {
            return emptyGenerator()
        }
        return when (param.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Long::class -> RandomLongGenerator(param)
            String::class -> RandomStringGenerator(param)
            Double::class -> RandomDoubleGenerator(param)
            List::class -> RandomListGenerator(param)
            Map::class -> RandomMapGenerator(param)
            else -> throw NotImplementedError(
                "Parameters with specs other than " +
                        "integer, number, or string are not yet implemented."
            )
        }
    }

    private class emptyGenerator() : Sequence<String> {
        override fun iterator(): Iterator<String> = object : Iterator<String> {
            override fun hasNext(): Boolean {
                return true
            }

            override fun next(): String {
                return ""
            }
        }
    }

    private class RandomLongGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<Long>(
            param,
            { 0L },
            { faker.number().randomNumber() },
            { p -> faker.number().numberBetween(p.minInt, p.maxInt) }
        )

    private class RandomDoubleGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<Double>(
            param,
            { 0.0 },
            { faker.number().randomDouble(5, -10000000, 10000000) },
            { p -> threadLocalRandom.nextDouble(p.minDecimal, p.maxDecimal) }
        )

    private class RandomStringGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<String>(
            param,
            { "" },
            { faker.regexify("[A-z1-9]{0,25}") },
            { p ->
                if (p.passingValues.isEmpty()) {
                    faker.regexify("[A-z1-9]{0,25}")
                } else {
                    p.passingValues.random().toString()
                }
            }
        )

    private class RandomMapGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<Map<String, Any>>(
            param,
            { mapOf() },
            SimpleInputGenerator::streamMapParamSequence,
            SimpleInputGenerator::streamMapParamSequence //TODO finish algorithm
        )

    private class RandomListGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<List<Any>>(
            param,
            { listOf() },
            SimpleInputGenerator::streamListParamSequence,
            SimpleInputGenerator::streamListParamSequence //TODO finish algorithm
        )

    abstract class RandomBaseGenerator<T>(
        val param: ParamInfo<Any>,
        val nullFun: (ParamInfo<Any>) -> T,
        val invalidFun: (ParamInfo<Any>) -> T,
        val validFun: (ParamInfo<Any>) -> T,
    ) : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {

            override fun next(): T {
                val percentNull = .15
                val percentInvalid = .10
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


