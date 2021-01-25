package org.shaper.global.results

import kotlinx.serialization.json.*
import org.shaper.generators.model.ResponseData
import org.shaper.serialization.JsonTree
import org.shaper.swagger.constants.JsonProperties
import org.shaper.swagger.model.ResponseBodySpec

object ResultsFieldsGlobal {
    lateinit var index: MutableMap<String, MutableSet<JsonPrimitive>>
    lateinit var multiIndex: MutableMap<String, MutableSet<JsonPrimitive>>
    //todo handle arrays and maps

    fun getFromKey(key: String): MutableSet<JsonPrimitive>{
        return if (this::index.isInitialized){
            index[key] ?: mutableSetOf()
        }else {
            mutableSetOf()
        }
    }

    // Call this if you want to inject previous data or test data into these globals
    fun initGlobals(
        _index: MutableMap<String, MutableSet<JsonPrimitive>> = mutableMapOf(),
        _multiIndex: MutableMap<String, MutableSet<JsonPrimitive>> = mutableMapOf(),
        reset: Boolean = false
    ) {
        if (!this::index.isInitialized || reset) {
            index = _index
            multiIndex = _multiIndex
        }
    }

    fun save(responseData: ResponseData, responseBodySpec: ResponseBodySpec) {
        initGlobals()
        JsonTree.traverse(
            jsonElement = responseData.bodyParsed,
            terminalFunction = this::saveResultField
        )
    }

    private fun saveResultField(fieldName: String, fullPath: String, value: JsonPrimitive) {
        saveResultFieldImpl(index, fieldName, fullPath, value) { list: MutableSet<JsonPrimitive>, any: JsonPrimitive ->
            list.add(any)
        }
        //todo - multi index and object index
//        if (value is List<*> || value is Set<*>) {
//            saveResultFieldImpl(multiIndex, fieldName, fullPath, value) { list: MutableSet<Any>, any: Any ->
//                list.addAll(any as Collection<Any>)
//            }
//        }

    }

    private fun saveResultFieldImpl(
        idx: MutableMap<String, MutableSet<JsonPrimitive>>,
        fieldName: String,
        path: String,
        value: JsonPrimitive,
        addFun: (MutableSet<JsonPrimitive>, JsonPrimitive) -> Unit
    ) {
        // normalize keys for indexing
        val pathKey = (path + fieldName).toLowerCase()
        val key = fieldName.toLowerCase()
        // index by just field name
        val valuesList = idx.getOrPut(key) { mutableSetOf() }
        addFun(valuesList, value)
        // if the path and field name are not the same, send to another index with the full path
        if (pathKey != fieldName) {
            val valuesListExtended = idx.getOrPut(pathKey) { mutableSetOf() }
            addFun(valuesListExtended, value) //TODO what about last 2 of 3  in path chain?
        }
    }

}