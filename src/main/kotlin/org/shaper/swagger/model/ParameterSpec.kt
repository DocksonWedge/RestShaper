package org.shaper.swagger.model


import io.swagger.v3.oas.models.parameters.Parameter
import propCheck.arbitrary.Gen
import java.util.*
import kotlin.reflect.KClass


class ParameterSpec(
    private val param: Parameter
) {

    //TODO
    val dataType: KClass<*> = when (param.schema.type.toLowerCase()) {
        "string"    -> String::class //TODO - handle format param
        "number"    -> Double::class
        "integer"   -> Long::class
        "boolean"   -> Boolean::class
        "array"     -> List::class
        "object"    -> Map::class
        "uuid"      -> UUID::class
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

    lateinit var customGenerator: Gen<Any> //TODO
    lateinit var GeneratorEnum: List<String> //TODO

    val name = { param.name }
    val paramType = { param.`in` }
    val isID = {
         dataType == UUID::class
                 || param.name.contains("Id")
                 || param.name.contains("ID") //TODO how do we handle "id"?
                 || param.name.contains("_id", true)
                 || param.name.contains("id_", true)
                 || param.name.contains("-id", true)
                 || param.name.contains("id-", true)

    }
}
