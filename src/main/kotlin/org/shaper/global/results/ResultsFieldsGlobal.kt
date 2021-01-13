package org.shaper.global.results

import org.shaper.generators.model.ResponseData
import org.shaper.swagger.model.ResponseBodySpec

object ResultsFieldsGlobal {
    val index = mutableMapOf<String, MutableList<Any>>()
    val multiIndex = mutableMapOf<String, MutableList<Any>>()

    fun save(responseData: ResponseData, responseBodySpec: ResponseBodySpec){

    }

    private fun saveResultField(fieldName: String, pathTitle: String, value: Any) {
        saveResultFieldImpl(index, fieldName, pathTitle, value) { list: MutableList<Any>, any: Any ->
            list.add(any)
        }
        if (value is List<*> || value is Set<*>) {
            saveResultFieldImpl(multiIndex, fieldName, pathTitle, value) { list: MutableList<Any>, any: Any ->
                list.addAll(any as Collection<Any>)
            }
        }

    }

    private fun saveResultFieldImpl(
        idx: MutableMap<String, MutableList<Any>>,
        fieldName: String,
        pathTitle: String,
        value: Any,
        addFun: (MutableList<Any>, Any) -> Unit
    ) {
        val valuesList = idx.getOrPut(fieldName, { mutableListOf() })
        val valuesListExtended = idx.getOrPut(pathTitle.toLowerCase() + fieldName, { mutableListOf() })
        listOf(valuesList, valuesListExtended).forEach {
            addFun(it, value)
        }
    }
}