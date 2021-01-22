package org.shaper.global.results

import kotlinx.serialization.json.*
import org.shaper.generators.model.ResponseData
import org.shaper.swagger.constants.JsonProperties
import org.shaper.swagger.model.ResponseBodySpec

object ResultsFieldsGlobal {
    lateinit var index: MutableMap<String, MutableSet<Any>>
    lateinit var multiIndex: MutableMap<String, MutableSet<Any>>

    // Call this if you want to inject previous data or test data into these globals
    fun initGlobals(
        _index: MutableMap<String, MutableSet<Any>> = mutableMapOf(),
        _multiIndex: MutableMap<String, MutableSet<Any>> = mutableMapOf(),
        reset: Boolean = false
    ) {
        if(!this::index.isInitialized || reset) {
            index = _index
            multiIndex = _multiIndex
        }
    }

    fun save(responseData: ResponseData, responseBodySpec: ResponseBodySpec) {
        initGlobals()
        responseBodySpec.properties.forEach { property ->
            val splitProperty = JsonProperties.splitPropertyKey(property)
            saveResultField(
                splitProperty.last().toLowerCase(),
                property.toLowerCase(),
                getValue(splitProperty, responseData.bodyParsed)
            )
        }
    }


    private tailrec fun getValue(propertyKeys: List<String>, body: JsonElement): JsonElement {
        return if (body !is JsonObject) {
            when (body) {
                is JsonArray -> body.jsonArray
                is JsonNull -> body.jsonNull
                is JsonPrimitive -> body.jsonPrimitive
                else -> throw NoKnownResponseValueError(
                    "Could not figure out how to get a value from a response! key: $propertyKeys - body: $body"
                )
            }
        } else {
            getValue(
                propertyKeys.subList(1, propertyKeys.size),
                body.getOrDefault(propertyKeys[0], JsonNull)
            )
        }
    }

    private fun saveResultField(fieldName: String, fullPath: String, value: JsonElement) {
        saveResultFieldImpl(index, fieldName, fullPath, value) { list: MutableSet<Any>, any: Any ->
            list.add(any)
        }
        if (value is List<*> || value is Set<*>) {
            saveResultFieldImpl(multiIndex, fieldName, fullPath, value) { list: MutableSet<Any>, any: Any ->
                list.addAll(any as Collection<Any>)
            }
        }

    }

    private fun saveResultFieldImpl(
        idx: MutableMap<String, MutableSet<Any>>,
        fieldName: String,
        fullPath: String,
        value: JsonElement,
        addFun: (MutableSet<Any>, Any) -> Unit
    ) {
        val compressedFieldName = JsonProperties.compressPropertyKey(fullPath)
        val valuesList = idx.getOrPut(fieldName) { mutableSetOf() }
        val valuesListExtended = idx.getOrPut(compressedFieldName) { mutableSetOf() }
        addFun(valuesList, value.jsonPrimitive.content)
        if(compressedFieldName != fieldName) {
            addFun(valuesListExtended, value.jsonPrimitive.content) //TODO what about last 2 of 3  in path chain?
        }
    }


    private class NoKnownResponseValueError(message: String) : Exception(message)
}