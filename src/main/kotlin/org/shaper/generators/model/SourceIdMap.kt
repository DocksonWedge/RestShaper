package org.shaper.generators.model

class SourceIdMap() {
    private val sourceIdMap = mutableMapOf<String, String>()

    fun set(paramType: String, paramName: String, value: String?) {
        if (value == null || value.isBlank()) {
            sourceIdMap.remove(getKey(paramType, paramName))
        } else {
            sourceIdMap[getKey(paramType, paramName)] = value
        }
    }

    fun get(paramType: String, paramName: String): Any? {
        return sourceIdMap[getKey(paramType, paramName)]
    }

    fun values():
            MutableCollection<String>{
        return sourceIdMap.values
    }

    private fun getKey(paramType: String, paramName: String): String {
        return "${paramType.toUpperCase()}-${paramName.toUpperCase()}"
    }

}
