package org.shaper.swagger.model


import io.swagger.v3.oas.models.parameters.Parameter

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
        "array" -> List::class
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

    var maxNum = 100000000000L
    var minNum = -100000000000L
    val failingValues = mutableSetOf<Any>()
    val passingValues = mutableSetOf<Any>()

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
        if (value is Long) {
            val diffWithMax = maxNum - value
            val diffWithMin =  value - minNum
            if (diffWithMax > 0 && diffWithMin > 0){
                if(diffWithMax > diffWithMin){
                    minNum = value
                }else if(diffWithMax < diffWithMin){
                    maxNum = value
                }
            }
        } else if (value is String) {

        }
        failingValues.add(value)
    }
    fun addPassingValue(value: Any) {
        passingValues.add(value)
        failingValues.remove(value)
    }
}
