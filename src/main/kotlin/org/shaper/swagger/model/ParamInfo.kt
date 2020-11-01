package org.shaper.swagger.model

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import java.util.*
import kotlin.reflect.KClass

class ParamInfo<T>(schema: Schema<T>) {

    val dataType: KClass<*> = when (schema.type?.toLowerCase() ?: "") {
        "string" -> String::class //TODO - handle format param
        "number" -> Double::class
        "integer" -> Long::class
        "boolean" -> Boolean::class
        "array" -> String::class //List::class TODO- fix badness used for testing
        "object" -> Map::class
        "uuid" -> UUID::class
        else -> {
            String::class //TODO - reference schemas
        }
    }

    var maxInt = schema?.maximum?.toLong() ?: 10000000000L
    var minInt = schema?.minimum?.toLong() ?: -10000000000L
    val maxDecimal = schema?.maximum?.toDouble() ?: 10000000000.0
    val minDecimal = schema?.minimum?.toDouble() ?: -10000000000.0

    val failingValues = mutableSetOf<Any>()
    val passingValues = mutableSetOf<Any>()

    val nestedParams = mutableMapOf<String, ParamInfo<*>>()

    init {
        passingValues.addAll(getAllEnumValues(schema))
        if (dataType == Map::class) {
            nestedParams.putAll(getNestedParms(schema))
        }
    }

    private tailrec fun <Y> getAllEnumValues(schema: Schema<Y>): MutableSet<Any> {
        val setOfVals = mutableSetOf<Y>()
        setOfVals.addAll(schema.enum ?: listOf())
        return if (schema !is ArraySchema) {
            setOfVals as MutableSet<Any>
        } else {
            getAllEnumValues(schema.items)
        }
    }

    private fun <Y> getNestedParms(schema: Schema<Y>): MutableMap<String, ParamInfo<Any>> {
        // TODO - support maps?
        if (schema !is ObjectSchema) {
            throw error("Paramter schema ${schema.name} is not a map schema, but is on an object param.")
        }
        return schema
            .properties //todo add additional properties as well
            .filter { it.value != null }
            .mapValues { ParamInfo(it.value) }
            .toMutableMap()

    }

    val isID = { name: String ->
        dataType == UUID::class
                || name.matches(Regex("^.*(Id|ID).*$"))
                || name.matches(Regex("^.*[-_](id)[-_].*$"))
                || name.matches(Regex("^(id)*[-_].*|.*[-_](id)*\$"))
    }

//    // TODO tighten up return type
//    fun <Y> getAvailableValues(
//        iterations: Int,
//        endpoint: EndpointSpec,
//        getSelectedOrPreviousData: (EndpointSpec, ParamInfo<T>) -> List<Y>
//    ): List<Y> {
//        //TODO get values based on type
//        val values = listOf<Y>()
//        return values + getSelectedOrPreviousData(endpoint, this)
//    }

    fun addFailingValue(value: Any) {
        if (passingValues.contains(value)) return
        failingValues.add(value)
    }

    fun addPassingValue(value: Any) {
        passingValues.add(value)
        failingValues.remove(value)
    }
}

