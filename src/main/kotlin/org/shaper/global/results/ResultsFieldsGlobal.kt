package org.shaper.global.results

import kotlinx.serialization.json.*
import org.shaper.generators.model.ResponseData
import org.shaper.swagger.constants.JsonProperties
import org.shaper.swagger.model.ResponseBodySpec

object ResultsFieldsGlobal {
    val index = mutableMapOf<String, MutableList<Any>>()
    val multiIndex = mutableMapOf<String, MutableList<Any>>()

    fun save(responseData: ResponseData, responseBodySpec: ResponseBodySpec) {
        responseBodySpec.properties.forEach { property ->
            val properties = JsonProperties.splitPropertyKey(property)
            saveResultField(
                properties.last(),
                property,
                getValue(properties, responseData.bodyParsed)
            )
        }
    }


    private tailrec fun getValue(propertyKeys: List<String>, body: JsonElement): JsonElement {
        if (body !is JsonObject) {
            return when {
                body is JsonArray -> return body.jsonArray
                body is JsonNull -> return body.jsonNull
                body is JsonPrimitive -> body.jsonPrimitive
                else -> throw NoKnownResponseValueError(
                    "Could not figure out how to get a value from a response! key: $propertyKeys - body: $body"
                )
            }
        } else {
            return getValue(
                propertyKeys.subList(1, propertyKeys.size),
                body.getOrDefault(propertyKeys[0], JsonPrimitive(""))
            )
        }
    }

    private fun saveResultField(fieldName: String, fullPath: String, value: Any) {
        saveResultFieldImpl(index, fieldName, fullPath, value) { list: MutableList<Any>, any: Any ->
            list.add(any)
        }
        if (value is List<*> || value is Set<*>) {
            saveResultFieldImpl(multiIndex, fieldName, fullPath, value) { list: MutableList<Any>, any: Any ->
                list.addAll(any as Collection<Any>)
            }
        }

    }

    private fun saveResultFieldImpl(
        idx: MutableMap<String, MutableList<Any>>,
        fieldName: String,
        fullPath: String,
        value: Any,
        addFun: (MutableList<Any>, Any) -> Unit
    ) {
        val valuesList = idx.getOrPut(fieldName) { mutableListOf() }
        val valuesListExtended = idx.getOrPut(JsonProperties.compressPropertyKey(fullPath)) { mutableListOf() }
        listOf(valuesList, valuesListExtended).forEach {
            addFun(it, value)
        }
    }

    private class NoKnownResponseValueError(message: String) : Exception(message)
}