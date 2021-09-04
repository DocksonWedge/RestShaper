package org.shaper.generators.model

import mu.KotlinLogging

class SourceIdMap() {
    private val logger = KotlinLogging.logger {}
    enum class Type(val str: String) {
        QUERY("QUERY"),
        PATH("PATH"),
        HEADER("HEADER"),
        COOKIE("COOKIE"),
        BODY("BODY")
    }

    private val sourceIdMapList = mutableMapOf<Type, MutableMap<String, MutableMap<Int, String>>>()

    fun set(paramType: Type, paramName: String, position: Int, sourceId: String?) {
        if (sourceId == null || sourceId.isBlank()) {
            val list = sourceIdMapList[paramType]?.get(paramName)?.remove(position)
        } else {
            val sourceList = sourceIdMapList
                .getOrPut(paramType) { mutableMapOf() }
                .getOrElse(paramName) { mutableMapOf() }
            sourceList[position] = sourceId
            sourceIdMapList[paramType]?.put(paramName, sourceList)
        }
    }

    fun get(paramType: Type, paramName: String, position: Int): String? {
        return sourceIdMapList[paramType]?.get(paramName)?.get(position)
    }

    fun clear(paramType: Type){
        sourceIdMapList[paramType] = mutableMapOf()
    }

    fun getAllParamsAtType(paramType: Type, position: Int): Set<String> {
        return sourceIdMapList[paramType]?.mapNotNull {
            it.value[position]
        }?.toSet() ?: setOf()
    }

    fun convertParamsToPosition(paramType: Type, newPosition: Int, otherMap: SourceIdMap){
        val typeMap = this.sourceIdMapList[paramType] ?: return

        typeMap.forEach { nameEntry ->
            if (nameEntry.value.size > 1 ){
                logger.warn { "Found a parameter with too many source IDs! Ignoring all but the last! Param name: ${nameEntry.key}" }
            }
            nameEntry.value.forEach {
                otherMap.set(paramType, nameEntry.key, newPosition, it.value)
            }
        }
    }
}
