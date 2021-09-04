package org.shaper.global.results

import kotlinx.serialization.json.*
import org.shaper.generators.model.ResponseData
import org.shaper.generators.model.TestResult
import org.shaper.global.kafka.ResultsProducer
import org.shaper.serialization.JsonTree
import org.shaper.swagger.constants.JsonProperties
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ResponseBodySpec
import kotlin.concurrent.getOrSet


object ResultsFieldsGlobal {

    var index: ThreadLocal<MutableMap<String, MutableSet<Pair<JsonPrimitive, String>>>> = ThreadLocal()
    fun getIndex(): MutableMap<String, MutableSet<Pair<JsonPrimitive, String>>> {
        return index.getOrSet { mutableMapOf() }
    }
    var multiIndex: ThreadLocal<MutableMap<String, MutableSet<JsonPrimitive>>> = ThreadLocal()
    //todo handle arrays and maps

    fun getFromKey(key: String): MutableSet<Pair<JsonPrimitive, String>> {
        return index.getOrSet { mutableMapOf() }[key.toLowerCase()] ?: mutableSetOf()
    }

    // Call this if you want to inject previous data or test data into these globals
    fun initGlobals(
        _index: MutableMap<String, MutableSet<Pair<JsonPrimitive, String>>> = mutableMapOf(),
        _multiIndex: MutableMap<String, MutableSet<JsonPrimitive>> = mutableMapOf(),
        reset: Boolean = false
    ) {
        if (reset || index.get() == null) {
            index.set(_index)
            multiIndex.set(_multiIndex)
        }
    }

    fun save(testResult: TestResult) {
        initGlobals()
        JsonTree.traverse(
            jsonElement = testResult.response.bodyParsed,
            title = testResult.endpoint.title,
            terminalFunction = { fieldName: String, fullPath: String, title: String, value: JsonPrimitive
                ->
                this.saveResultField(fieldName, fullPath, title, value, testResult)
            }
        )
    }

    // TODO - in save, flush Endpoint plus field to DB
    private fun saveResultField(
        fieldName: String,
        fullPath: String,
        title: String,
        value: JsonPrimitive,
        testResult: TestResult
    ) {
        saveResultFieldImpl(
            index.get(),
            fieldName,
            fullPath,
            title,
            value
        ) { list: MutableSet<Pair<JsonPrimitive, String>>, paramVal: JsonPrimitive ->
            ResultsProducer.produceResultsFieldMessage(testResult, fieldName, fullPath, title, "BODY" ,value)
            list.add(paramVal to testResult.resultId)
        }
        //todo - multi index and object index
//        if (value is List<*> || value is Set<*>) {
//            saveResultFieldImpl(
        //            multiIndex,
        //            fieldName,
        //            fullPath,
        //            value
        //            ) { list: MutableSet<Any>, any: Any ->
//                list.addAll(any as Collection<Any>)
//            }
//        }

    }

    private fun saveResultFieldImpl(
        idx: MutableMap<String, MutableSet<Pair<JsonPrimitive, String>>>,
        fieldName: String,
        path: String,
        title: String = "",
        value: JsonPrimitive,
        addFun: (MutableSet<Pair<JsonPrimitive, String>>, JsonPrimitive) -> Unit
    ) {
        // normalize keys for indexing
        val pathKey = (path + fieldName).toLowerCase()
        val titleKey = (title + pathKey).toLowerCase()
        val key = fieldName.toLowerCase()
        // index by just field name
        val valuesList = idx.getOrPut(key) { mutableSetOf() }
        addFun(valuesList, value)
        // if the path and field name are not the same, send to another index with the full path
        if (pathKey != fieldName) {
            val valuesListExtended = idx.getOrPut(pathKey) { mutableSetOf() }
            addFun(valuesListExtended, value) //TODO what about last 2 of 3  in path chain?
        }
        if (titleKey != pathKey) {
            val valuesListExtended = idx.getOrPut(titleKey) { mutableSetOf() }
            addFun(valuesListExtended, value)
        }
    }

}