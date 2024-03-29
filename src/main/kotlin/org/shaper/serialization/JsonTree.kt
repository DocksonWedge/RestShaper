package org.shaper.serialization

import kotlinx.serialization.json.*
import org.shaper.global.results.ResultsFieldsGlobal

object JsonTree {
    fun traverse(
        jsonElement: JsonElement,
        currentKey: String = "",
        prevKeys: String = "",
        title: String = "",
        terminalFunction: (String, String, String, JsonPrimitive) -> Unit
    ) { // function to run when we hit the non-null terminal leaf
        when (jsonElement) {
            is JsonPrimitive -> terminatePrimitive(jsonElement, currentKey, prevKeys, title, terminalFunction)
            is JsonArray -> {
                jsonElement.jsonArray.forEach { value ->
                    traverse(
                        value,
                        currentKey, //keeping the same key for each list value
                        prevKeys,
                        title,
                        terminalFunction
                    )
                }
            }
            is JsonObject -> {
                jsonElement.jsonObject.forEach { (key, value) ->
                    traverse(
                        value,
                        key,
                        prevKeys + currentKey,
                        title,
                        terminalFunction
                    )
                }
            } //todo - handle explicit null?
        }
    }

    private fun terminatePrimitive(
        jsonElement: JsonElement,
        currentKey: String = "",
        prevKeys: String = "",
        title: String = "",
        terminalFunction: (String, String, String, JsonPrimitive) -> Unit
    ) {
        try {
            terminalFunction(currentKey, prevKeys, title, jsonElement.jsonPrimitive)
        } catch (e: Exception) {
            throw JsonTree.NoKnownResponseValueError(
                "Could not figure out how to get a value from a response! " +
                        "key: $prevKeys - $currentKey - jsonElement: $jsonElement " +
                        "error:${e.localizedMessage} \n ${e.stackTraceToString()}"
            )
        }
    }

    private class NoKnownResponseValueError(message: String) : Exception(message)
}