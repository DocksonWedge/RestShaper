package org.shaper.swagger.model

import kotlinx.serialization.json.JsonObject


// TODO user should be able to pass in their own function to add things like auth headers that they need to calculate
// Allows for nulls so we can "curry" the constructor
data class TestInput(
    var queryParams: Map<String, List<*>>,
    var pathParams: Map<String, List<*>>,
    var headers: Map<String, List<*>>,
    var cookies: Map<String, List<*>>,
    var bodies: List<JsonObject>
    // val additionalConfig: () -> Unit TODO - move this a level higher
) : Sequence<TestInputConcretion> { //TODO sequence instead?


    override fun iterator(): Iterator<TestInputConcretion> {
        return TestInputIterator(queryParams, pathParams, headers, cookies, bodies)
    }


    //TODO why do we have ot pass params explicitly? Is there ever an iterator that doesn't
    // need to know what it's iterating over?
    class TestInputIterator(
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
        ) //TODO actually use this to go to next? do we need an iterator?

        override fun hasNext(): Boolean {
            return true //currently an infinite collection
        }

        override fun next(): TestInputConcretion {
            position.queryParams = nextParam(queryParams, position.queryParams)
            if (!isParamReset(position.queryParams)) return inputFromPosition(position)

            position.pathParams = nextParam(pathParams, position.pathParams)
            if (!isParamReset(position.pathParams)) return inputFromPosition(position)

            position.headers = nextParam(headers, position.headers)
            if (!isParamReset(position.headers)) return inputFromPosition(position)

            position.cookies = nextParam(cookies, position.cookies)
            return inputFromPosition(position)

        }

        //returns position of set of parameters
        private fun nextParam(
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

        private fun getParamValue(params: Map<String, List<*>>, position: MutableMap<String, Int>): Map<String, *> {
            return params.mapValues {
                it.value[
                        position[it.key]
                            ?: error("Iterating over a parameter that doesn't exist!")
                ]
            }
        }

        private fun isParamReset(position: MutableMap<String, Int>): Boolean {
            return position.all { it.value == 0 }
        }

        private fun inputFromPosition(position: IterPosition): TestInputConcretion {
            return TestInputConcretion(
                getParamValue(queryParams, position.queryParams),
                getParamValue(pathParams, position.pathParams),
                getParamValue(headers, position.headers),
                getParamValue(cookies, position.cookies),
                JsonObject(mapOf()) //TODO
            )
        }

    }


    data class IterPosition(
        var queryParams: MutableMap<String, Int>,
        var pathParams: MutableMap<String, Int>,
        var headers: MutableMap<String, Int>,
        var cookies: MutableMap<String, Int>,
        var bodies: Int
    )


}