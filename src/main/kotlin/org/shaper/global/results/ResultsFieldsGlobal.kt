package org.shaper.global.results

object ResultsFieldsGlobal {
    val index = mutableMapOf<String, MutableList<Any>>()
    val multiIndex = mutableMapOf<String, MutableList<Any>>()
    fun saveResultField(fieldName: String, pathTitle: String, value: Any) {
        saveImpl(index, fieldName, pathTitle, value) { list: MutableList<Any>, any: Any ->
            list.add(any)
        }
        if (value is List<*> || value is Set<*>) {
            saveImpl(multiIndex, fieldName, pathTitle, value) { list: MutableList<Any>, any: Any ->
                list.addAll(any as Collection<Any>)
            }
        }

    }

    private fun saveImpl(
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