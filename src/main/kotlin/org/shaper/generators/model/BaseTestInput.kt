package org.shaper.generators.model

import kotlinx.serialization.json.JsonObject
import org.shaper.generators.shared.IterPosition

// TODO - adding in weights/priorities?
// TODO user should be able to pass in their own function to add things like auth headers that they need to calculate
// Allows for nulls so we can "curry" the constructor
abstract class BaseTestInput(
    var queryParams: Map<String, List<*>>,
    var pathParams: Map<String, List<*>>,
    var headers: Map<String, List<*>>,
    var cookies: Map<String, List<*>>,
    var bodies: List<JsonObject>
    // val additionalConfig: () -> Unit TODO - move this a level higher
) : Sequence<TestInputConcretion> { //TODO sequence instead?

    abstract override fun iterator(): Iterator<TestInputConcretion>


    //TODO why do we have ot pass params explicitly? Is there ever an iterator that doesn't
    // need to know what it's iterating over?
    abstract class BaseTestInputIterator(
        var queryParams: Map<String, List<*>>,
        var pathParams: Map<String, List<*>>,
        var headers: Map<String, List<*>>,
        var cookies: Map<String, List<*>>,
        var bodies: List<JsonObject> // TODO - should probably be map of maps
        // val additionalConfig: () -> Unit TODO - move this a level higher?
    ) : Iterator<TestInputConcretion> {
        var position = IterPosition(
            queryParams.mapValues { 0 }.toMutableMap(),
            pathParams.mapValues { 0 }.toMutableMap(),
            headers.mapValues { 0 }.toMutableMap(),
            cookies.mapValues { 0 }.toMutableMap(),
            0
        )

        abstract override fun hasNext(): Boolean
        abstract override fun next(): TestInputConcretion

        //returns position of set of parameters
        protected open fun nextParam(
            params: Map<String, List<*>>,
            position: MutableMap<String, Int>
        ): MutableMap<String, Int> {
            params.forEach { param ->
                val currentPos = position[param.key] ?: error("Iterating over a parameter that doesn't exist!")
                if (currentPos == param.value.size - 1) {
                    position[param.key] = 0
                    //and we continue since we are doing duplicates!
                } else {
                    //the first one we need to increment, we are done!
                    position[param.key] = position[param.key]!! + 1
                    return position
                }
            }
            return position
        }

        protected fun getParamValue(params: Map<String, List<*>>, position: MutableMap<String, Int>): Map<String, *> {
            return params.mapValues {param ->
                param.value[
                        position[param.key]
                            ?: error("Iterating over a parameter that doesn't exist!")
                ]
            }
        }

        protected open fun isParamReset(position: MutableMap<String, Int>): Boolean {
            return position.all { it.value == 0 }
        }

        protected fun inputFromPosition(position: IterPosition): TestInputConcretion {
            return TestInputConcretion(
                getParamValue(queryParams, position.queryParams),
                getParamValue(pathParams, position.pathParams),
                getParamValue(headers, position.headers),
                getParamValue(cookies, position.cookies),
                JsonObject(mapOf()) //TODO
            )
        }

    }
}