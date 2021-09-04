package org.shaper.generators.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
    var bodies: Sequence<*>,
    val sourceIdMap: SourceIdMap
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
        var bodies: Sequence<*>, // TODO - should probably be map of maps
        val sourceIdMap: SourceIdMap
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
            bodies.iterator().withIndex()
        )

        protected fun inputFromPosition(position: IterPosition, sourceIdMap: SourceIdMap): TestInputConcretion {
            val sourceIds = mutableSetOf<String>()
            val concretion = TestInputConcretion(
                getParamValuesAtPosition(queryParams, position.queryParams, SourceIdMap.Type.QUERY, sourceIdMap, sourceIds),
                getParamValuesAtPosition(pathParams, position.pathParams, SourceIdMap.Type.PATH, sourceIdMap, sourceIds),
                getParamValuesAtPosition(headers, position.headers, SourceIdMap.Type.HEADER, sourceIdMap, sourceIds),
                getParamValuesAtPosition(cookies, position.cookies, SourceIdMap.Type.COOKIE, sourceIdMap, sourceIds),
                getBodyValuesAtPosition(position.bodiesIter, sourceIdMap, sourceIds)//TODO
            )
            concretion.sourceResultIds.addAll(sourceIds)
            return concretion
        }

        private fun getParamValuesAtPosition(
            params: Map<String, Sequence<*>>,
            position: MutableMap<String, ParamPosition<*>>,
            paramType: SourceIdMap.Type,
            sourceIdMap: SourceIdMap,
            sourceIds: MutableSet<String>
        ): Map<String, *> {
            return params.mapValues { param ->
                val inputPosition = position[param.key] ?: error("Iterating over a parameter that doesn't exist!")
                sourceIdMap.get(paramType, param.key, inputPosition.index)?.let { sourceIds.add(it) }
                inputPosition.currentVal
            }
        }

        private fun getBodyValuesAtPosition(
            position: Iterator<IndexedValue<*>>,
            sourceIdMap: SourceIdMap,
            sourceIds: MutableSet<String>
        ): JsonElement {
            val indexedVal = position.next()
            val nextVal = indexedVal.value ?: ""
            sourceIds.addAll(sourceIdMap.getAllParamsAtType(SourceIdMap.Type.BODY, indexedVal.index))
            return if (nextVal is Map<*, *>) {
                JsonObject(nextVal as Map<String, JsonElement>)
            } else if (nextVal is List<*>) {
                JsonArray(nextVal as List<JsonElement>)
            } else if (nextVal is String) {
                JsonPrimitive(nextVal as String)
            } else {
                JsonPrimitive("")
            }
        }


    }
}