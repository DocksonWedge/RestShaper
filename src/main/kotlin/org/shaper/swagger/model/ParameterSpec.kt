package org.shaper.swagger.model


import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.parameters.Parameter
import java.math.BigDecimal

import java.util.*
import kotlin.reflect.KClass


class ParameterSpec(
    private val param: Parameter
) {

    //TODO - add as generic type somehow
    val dataType: KClass<*> = when (param.schema.type.toLowerCase()) {
        "string" -> String::class //TODO - handle format param
        "number" -> Double::class
        "integer" -> Long::class
        "boolean" -> Boolean::class
        "array" -> String::class //List::class TODO- fix badness used for testing
        "object" -> Map::class
        "uuid" -> UUID::class
        //TODO - does style matter here if type is exhaustive?
        else -> {
            when (param.style) {
                Parameter.StyleEnum.DEEPOBJECT -> Map::class
                Parameter.StyleEnum.SIMPLE -> String::class
                //TODO break down enum into a list
                else -> List::class
            }
        }
    }
    val name: String = param.name
    val paramType: String = param.`in`

    var maxInt = param.schema?.maximum?.toLong() ?: 10000000000L
    var minInt = param.schema?.minimum?.toLong() ?: -10000000000L
    val maxDecimal = param.schema?.maximum ?: BigDecimal(10000000000)
    val minDecimal = param.schema?.minimum ?: BigDecimal(-10000000000)

    val failingValues = mutableSetOf<Any>()
    val passingValues = mutableSetOf<Any>()
    init{
        passingValues.addAll(param.schema?.enum ?: listOf())
        //TODO - recursively go down the nested schemas
        if (param.schema is ArraySchema) {
            passingValues.addAll((param.schema as ArraySchema).items?.enum ?: listOf())
        }
    }

    val isID = (
            dataType == UUID::class
                    || name.matches(Regex("^.*(Id|ID).*$"))
                    || name.matches(Regex("^.*[-_](id)[-_].*$"))
                    || name.matches(Regex("^(id)*[-_].*|.*[-_](id)*\$"))
            )

    // TODO tighten up return type
    fun <T> getAvailableValues(
        iterations: Int,
        endpoint: EndpointSpec,
        getSelectedOrPreviousData: (EndpointSpec, ParameterSpec) -> List<T>
    ): List<T> {
        //TODO get values based on type
        val values = listOf<T>()
        return values + getSelectedOrPreviousData(endpoint, this)
    }

    fun addFailingValue(value: Any) {
        if (passingValues.contains(value)) return
        failingValues.add(value)
    }
    fun addPassingValue(value: Any) {
        passingValues.add(value)
        failingValues.remove(value)
    }
}
