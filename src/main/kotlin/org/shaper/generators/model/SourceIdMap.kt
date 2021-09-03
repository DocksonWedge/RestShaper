package org.shaper.generators.model

class SourceIdMap() {
    private val sourceIdMapList = mutableMapOf<String, MutableList<String>>()

    fun set(paramType: String, paramName: String, position: Int, sourceId: String?) {
        if (sourceId == null || sourceId.isBlank()) {
            sourceIdMapList[getKey(paramType, paramName)]?.removeAt(position)
        } else {
            val sourceList = sourceIdMapList.getOrElse(getKey(paramType, paramName)) { mutableListOf() }
            sourceList[position] = sourceId
            sourceIdMapList[getKey(paramType, paramName)] = sourceList
        }
    }

    fun get(paramType: String, paramName: String, position: Int): Any? {
        return sourceIdMapList[getKey(paramType, paramName)]?.get(position)
    }

    fun values(position: Int): Collection<String> {
        return sourceIdMapList.map { it.value[position] }
    }

    private inline fun getKey(paramType: String, paramName: String): String {
        return "${paramType.toUpperCase()}-${paramName.toUpperCase()}"
    }

}
