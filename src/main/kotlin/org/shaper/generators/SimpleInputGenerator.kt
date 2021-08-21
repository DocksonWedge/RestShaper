package org.shaper.generators

import com.github.javafaker.Faker
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.shaper.swagger.model.EndpointSpec
import org.shaper.generators.model.SimpleTestInput
import org.shaper.generators.model.SourceIdMap
import org.shaper.generators.model.StaticParams
import org.shaper.global.results.ResultsStateGlobal
import org.shaper.swagger.model.ParamInfo
import org.shaper.swagger.model.ParameterSpec
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

//simple doesn't read past results?
class SimpleInputGenerator(
    val numCases: Int = 25,
    val additionalConfig: (EndpointSpec, SimpleTestInput) -> Unit
    = { endpointSpec: EndpointSpec, testInput: SimpleTestInput -> }
) {
    companion object {
        //instantiate this as little as possible using a static companion - it is apparently very slow
        private val faker = Faker()
        private val valueReusePercent = .7
        val threadLocalRandom = ThreadLocalRandom.current()

        private fun randPercent(): Double {
            return faker.number().randomDouble(3, 0, 1)
        }

        fun getStreamMapParamSequence(param: ParamInfo<Any>, sourceIdMap: SourceIdMap)
                : (param: ParamInfo<Any>) -> Map<String, Any> {
            //TODO map any could be json elements
            return fun (param: ParamInfo<Any>): Map<String, Any> {
                //TODO add required fields handling
                return param
                    .nestedParams
                    .mapValues {
                        SimpleInputGenerator()
                            .getParamVals(it.value as ParamInfo<Any>, sourceIdMap)
                            .iterator()
                            .next() ?: ""
                    }
            }
        }
        fun getStreamListParamSequence(param: ParamInfo<Any>, sourceIdMap: SourceIdMap)
                : (param: ParamInfo<Any>) -> List<Any> {
            return fun (param: ParamInfo<Any>): List<Any> {
                //TODO add max length handling
                return (1..faker.number().numberBetween(1, 5)).map { _ ->
                    SimpleInputGenerator()
                        .getParamVals(param.listParam, sourceIdMap)
                        .iterator()
                        .next() ?: ""
                }
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
            } else if (poko is JsonElement) {
                poko
            } else {
                throw NotImplementedError(
                    "When converting to JSON, " +
                            "found a data type that we could not parse: $poko of type ${poko::class}"
                )
            }
        }

        private fun <T> getPassingNumber(
            conversionFun: Number.() -> T,
            sourceIdMap: SourceIdMap,
            randomFun: (ParamInfo<Any>) -> T,
        ): (ParamInfo<Any>) -> T {
            return { p ->
                val passingValues =
                    p.passingValues // this "get" does a recalculation every time, so try not to call it directly
                if (passingValues.isEmpty() || valueReusePercent < randPercent()) {
                    randomFun(p)
                } else {
                    val chosenVal = passingValues.random()
                    val value = chosenVal.first
                    sourceIdMap.set(p.paramType, p.name, chosenVal.second)
                    when (value) {
                        is Number -> value.conversionFun()
                        is String -> {
                            try {
                                value.toBigDecimal().conversionFun()
                            } catch (nfe: NumberFormatException) {
                                randomFun(p)
                            }
                        }
                        else -> randomFun(p)
                    }
                }
            }
        }
    }

    fun getInput(endpoint: EndpointSpec, staticParams: StaticParams): SimpleTestInput {
        val sourceIdMap = SourceIdMap()
        return SimpleTestInput(
            endpoint.queryParams.mapValues {
                getRandomOrStaticParamVals(it, staticParams.queryParams, sourceIdMap)
            },
            endpoint.pathParams.mapValues {
                getRandomOrStaticParamVals(it, staticParams.pathParams, sourceIdMap)
            },
            endpoint.headerParams.mapValues {
                getRandomOrStaticParamVals(it, staticParams.headers, sourceIdMap)
            },
            endpoint.cookieParams.mapValues {
                getRandomOrStaticParamVals(it, staticParams.cookies, sourceIdMap)
            },
            getBodyVals(endpoint.requestBody.bodyInfo, sourceIdMap),
            numCases,
            sourceIdMap
        )
    }

    private fun getRandomOrStaticParamVals(
        paramDef: Map.Entry<String, ParameterSpec>,
        staticParam: Map<String, Any?>,
        sourceIdMap: SourceIdMap
    ): Sequence<*> {
        sourceIdMap.set(paramDef.value.info.paramType, paramDef.value.info.name, null)
        if (staticParam.containsKey(paramDef.key)) { // TODO add to other params
            return SingleValueForever(staticParam[paramDef.key])
        } else {
            return getParamVals(paramDef.value.info, sourceIdMap)
        }
    }

    fun getBodyVals(param: ParamInfo<Any>?, sourceIdMap: SourceIdMap): Sequence<*> {
        return sequence {
            val iter = getParamVals(param, sourceIdMap).iterator()
            while (iter.hasNext()) {
                yield(pokoToJsonElement(iter.next() ?: ""))
            }
        }
    }

    // TODO calculate function based on requirements, then pass it in
    // TODO can we have a way here to know when we have done something invalid and push that to the expected results
    fun getParamVals(param: ParamInfo<Any>?, sourceIdMap: SourceIdMap): Sequence<*> {
        if (param == null) {
            return emptyGenerator()
        }
        return when (param.dataType) {
            // TODO - more complex generator that hits edge cases and is aware of parameter spec
            Long::class -> RandomLongGenerator(param, sourceIdMap)
            String::class -> RandomStringGenerator(param, sourceIdMap)
            Double::class -> RandomDoubleGenerator(param, sourceIdMap)
            Boolean::class -> RandomBooleanGenerator(param)
            List::class -> RandomListGenerator(param, sourceIdMap)
            Map::class -> RandomMapGenerator(param, sourceIdMap)
            else -> throw NotImplementedError(
                "Parameters with data type other than " +
                        "integer, number, boolean, or string are not yet implemented."
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

    private class RandomLongGenerator(param: ParamInfo<Any>, sourceIdMap: SourceIdMap) :
        RandomBaseGenerator<Long>(
            param,
            { 0L },
            { faker.number().randomNumber() },
            getPassingNumber(Number::toLong, sourceIdMap) { p -> faker.number().numberBetween(p.minInt, p.maxInt) }
        )

    private class RandomBooleanGenerator(param: ParamInfo<Any>) :
        RandomBaseGenerator<Boolean>(
            param,
            { Random.nextBoolean() },
            { Random.nextBoolean() },
            { Random.nextBoolean() }
        )

    private class RandomDoubleGenerator(param: ParamInfo<Any>, sourceIdMap: SourceIdMap) :
        RandomBaseGenerator<Double>(
            param,
            { 0.0 },
            { faker.number().randomDouble(5, -10000000, 10000000) },
            getPassingNumber(Number::toDouble, sourceIdMap) { p ->
                threadLocalRandom.nextDouble(
                    p.minDecimal,
                    p.maxDecimal
                )
            }
        )

    private class RandomStringGenerator(param: ParamInfo<Any>, sourceIdMap: SourceIdMap) :
        RandomBaseGenerator<String>(
            param,
            { "" },
            { faker.regexify("[A-z1-9]{0,25}") },
            { p ->
                if (p.passingValues.isEmpty() || valueReusePercent < randPercent()) {
                    faker.regexify("[A-z1-9]{0,25}")
                } else {
                    val chosenVal = p.passingValues.random()
                    sourceIdMap.set(param.paramType, param.name, chosenVal.second)
                    chosenVal.first.toString()
                }
            }
        )

    private class RandomMapGenerator(param: ParamInfo<Any>, sourceIdMap: SourceIdMap) :
        RandomBaseGenerator<Map<String, Any>>(
            param,
            { mapOf() },
            SimpleInputGenerator.getStreamMapParamSequence(param, sourceIdMap),
            SimpleInputGenerator.getStreamMapParamSequence(param, sourceIdMap) //TODO finish algorithm
        )

    private class RandomListGenerator(param: ParamInfo<Any>, sourceIdMap: SourceIdMap) :
        RandomBaseGenerator<List<Any>>(
            param,
            { listOf() },
            SimpleInputGenerator.getStreamListParamSequence(param, sourceIdMap),
            SimpleInputGenerator.getStreamListParamSequence(param, sourceIdMap) //TODO finish algorithm
        )

    abstract class RandomBaseGenerator<T>(
        val param: ParamInfo<Any>,
        val nullFun: (ParamInfo<Any>) -> T,
        val invalidFun: (ParamInfo<Any>) -> T,
        val validFun: (ParamInfo<Any>) -> T,
    ) : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {

            override fun next(): T {
                val percentNull = .05
                val percentInvalid = .05
                val randomNum = randPercent()
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

    class SingleValueForever<T>(
        val element: T
    ) : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun next(): T {
                return element
            }

            override fun hasNext(): Boolean {
                return true
            }
        }
    }
}


