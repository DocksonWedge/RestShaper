package org.shaper.generators.model

import kotlinx.serialization.json.JsonObject
import org.shaper.generators.shared.IterPosition
import org.shaper.generators.shared.ParamPosition

// TODO - adding in weights/priorities?
// TODO user should be able to pass in their own function to add things like auth headers that they need to calculate
// Allows for nulls so we can "curry" the constructor
abstract class BaseTestInput(
    var queryParams: Map<String, Sequence<*>>,
    var pathParams: Map<String, Sequence<*>>,
    var headers: Map<String, Sequence<*>>,
    var cookies: Map<String, Sequence<*>>,
    var bodies: Sequence<JsonObject>
    // val additionalConfig: () -> Unit TODO - move this a level higher
) : Sequence<TestInputConcretion> {

    abstract override fun iterator(): Iterator<TestInputConcretion>


    //TODO why do we have ot pass params explicitly? Is there ever an iterator that doesn't
    // need to know what it's iterating over?
    abstract class BaseTestInputIterator(
        var queryParams: Map<String, Sequence<*>>,
        var pathParams: Map<String, Sequence<*>>,
        var headers: Map<String, Sequence<*>>,
        var cookies: Map<String, Sequence<*>>,
        var bodies: Sequence<JsonObject> // TODO - should probably be map of maps
        // val additionalConfig: () -> Unit TODO - move this a level higher?
    ) : Iterator<TestInputConcretion> {

        val getInitPosition =
            { entry: Map.Entry<String, Sequence<*>> -> entry.value.iterator() }

        // Start as -1 to run at least once
        var position = IterPosition(
            queryParams.mapValues { getInitPosition(it) }.toMutableMap(),
            pathParams.mapValues { getInitPosition(it) }.toMutableMap(),
            headers.mapValues { getInitPosition(it) }.toMutableMap(),
            cookies.mapValues { getInitPosition(it) }.toMutableMap(),
            sequenceOf<Any>().iterator()
        )

        protected fun inputFromPosition(position: IterPosition): TestInputConcretion {
            return TestInputConcretion(
                getParamValuesAtPosition(queryParams, position.queryParams),
                getParamValuesAtPosition(pathParams, position.pathParams),
                getParamValuesAtPosition(headers, position.headers),
                getParamValuesAtPosition(cookies, position.cookies),
                JsonObject(mapOf()) //TODO
            )
        }

        private fun getParamValuesAtPosition(
            params: Map<String, Sequence<*>>,
            position: MutableMap<String, ParamPosition<*>>
        ): Map<String, *> {
            return params.mapValues { param ->
                val inputPosition = position[param.key] ?: error("Iterating over a parameter that doesn't exist!")
                inputPosition.currentVal
            }
        }

    }
}