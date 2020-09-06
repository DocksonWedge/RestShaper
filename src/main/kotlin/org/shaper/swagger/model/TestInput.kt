package org.shaper.swagger.model

import arrow.core.Tuple5
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
) : Iterable<TestInputConcretion> { //TODO sequence instead?


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
        var position = mutableListOf(
            queryParams.mapValues { 0 }.toMutableMap(),
            pathParams.mapValues { 0 }.toMutableMap(),
            headers.mapValues { 0 }.toMutableMap(),
            cookies.mapValues { 0 }.toMutableMap(),
            0
        ) //TODO actually use this to go to next? do we need an iterator?

        override fun hasNext(): Boolean {
            TODO("Not yet implemented")
        }

        override fun next(): TestInputConcretion {
            TODO("Not yet implemented")
        }

        private fun nextFinder(increment: Boolean = false): TestInputConcretion {
            TODO("Not yet implemented")
            // alg - tail recursion go through each parameter if last parameter position is last reset to 0, otherwise increment
            //  if the next parameter was set to 0 or DNE increment current, pop back up. If incremented DNE, reset to 0.
//            if (position[5] < bodies.size) {
//
//                if (increment) position[5]++
//            } else {
//
//            }
//            position.d >= cookies.size -> 1
//            position.c >= headers.size -> 1
//            position.b >= pathParams.size -> 1
//            position.a >= queryParams.size -> 1

        }

    }

}