package org.shaper.generators.model

import kotlinx.serialization.json.JsonObject
import org.shaper.generators.shared.IterPosition
import org.shaper.generators.shared.ParamPosition

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
) : Sequence<TestInputConcretion> {

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

        val getInitPosition =
            { entry: Map.Entry<String, List<*>> ->
                if (entry.value.isEmpty()) sequenceOf<Any>().iterator() else entry.value.listIterator()
            }

        // Start as -1 to run at least once
        var position = IterPosition(
            queryParams.mapValues { getInitPosition(it) }.toMutableMap(),
            pathParams.mapValues { getInitPosition(it) }.toMutableMap(),
            headers.mapValues { getInitPosition(it) }.toMutableMap(),
            cookies.mapValues { getInitPosition(it) }.toMutableMap(),
            sequenceOf<Any>().iterator()
        )

        abstract override fun hasNext(): Boolean
        abstract override fun next(): TestInputConcretion

        //returns position of set of parameters
        protected open fun nextParam(
            position: MutableMap<String, ParamPosition<*>>,
            paramVals: Map<String, Iterable<*>>
        ): MutableMap<String, ParamPosition<*>> {
            position.forEach { param ->
                val paramPos = param.value
                if (!paramPos.hasNext()) {
                    position[param.key] = ParamPosition(
                        (paramVals[param.key] ?: error("Mismatched param key with position: ${param.key}"))
                            .iterator()
                    )
                    //and we continue since we are doing duplicates!
                } else {
                    //the first one we need to increment, we are done!
                    paramPos.next()
                    return position
                }
            }
            return position
        }

        protected fun getParamValuesAtPosition(
            params: Map<String, List<*>>,
            position: MutableMap<String, ParamPosition<*>>
        ): Map<String, *> {
            return params.mapValues { param ->
                val inputPosition = position[param.key] ?: error("Iterating over a parameter that doesn't exist!")
                inputPosition.currentVal
            }
        }

        protected open fun isParamReset(
            position: MutableMap<String, ParamPosition<*>>
        ): Boolean {
            return position.all {
                val paramPosition = it.value
                paramPosition.hasNext() //only reset if we are back at 0
                        && paramPosition.wasReset //only reset if we changed values
                        && !paramPosition.isEmpty //never reset if no values to reset
            }
        }

        protected open fun isOnLastParam(
            position: MutableMap<String, ParamPosition<*>>
        ): Boolean {
            return position.all {
                !it.value.hasNext()
            }
        }

        protected open fun hasNoValues(paramVals: Map<String, List<*>>): Boolean {
            return paramVals.isEmpty() || paramVals.all { it.value.isEmpty() }
        }

        protected fun inputFromPosition(position: IterPosition): TestInputConcretion {
            return TestInputConcretion(
                getParamValuesAtPosition(queryParams, position.queryParams),
                getParamValuesAtPosition(pathParams, position.pathParams),
                getParamValuesAtPosition(headers, position.headers),
                getParamValuesAtPosition(cookies, position.cookies),
                JsonObject(mapOf()) //TODO
            )
        }

    }
}