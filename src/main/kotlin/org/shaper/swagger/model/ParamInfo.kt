package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import org.shaper.global.results.ResultsFieldsGlobal
import java.util.*
import kotlin.reflect.KClass

class ParamInfo<T>(private val _schema: Schema<T>, private val fullSpec: OpenAPI, val name: String = "", val paramType: String) {

    val schema = deriveSchema(_schema)
    val dataType: KClass<*> = swaggerTypeToKClass(schema.type?.toLowerCase() ?: "")

    val maxInt = schema?.maximum?.toLong() ?: 10000000000L
    val minInt = schema?.minimum?.toLong() ?: -10000000000L
    val maxDecimal = schema?.maximum?.toDouble() ?: 10000000000.0
    val minDecimal = schema?.minimum?.toDouble() ?: -10000000000.0

    val failingValues = mutableSetOf<Any>()
    val passingValues = mutableSetOf<Pair<Any, String>>()
        get() {
            // so.... schema name and title are null alot... not sure why
            if (name.isNotBlank()) { //TODO - trigger delegate listener?
                field.addAll(
                    ResultsFieldsGlobal //TODO - do we care if there is a type mismatch? i.e. we pass an int in json for a string?
                        .getFromKey(name)
                        .map { // only add if we are type safe here
//                            if (it.content::class == dataType) {
                            it.first.content to it.second
//                            } else {
//                                null
//                            }
                        })
            }
            return field
        }


    val nestedParams = mutableMapOf<String, ParamInfo<*>>()

    var listParam: ParamInfo<Any>? = null

    init {
        passingValues.addAll(getAllEnumValues(schema).map{ it to "" }.toMutableSet())
        when (dataType) {
            Map::class ->
                nestedParams.putAll(getNestedParms(schema))
            List::class ->
                listParam = ParamInfo((schema as ArraySchema).items as Schema<Any>, fullSpec, name, paramType)
        }
    }

    private fun swaggerTypeToKClass(type: String): KClass<*> {
        return when (type) {
            "string" -> String::class //TODO - handle format param
            "number" -> Double::class
            "integer" -> Long::class
            "boolean" -> Boolean::class
            "array" -> List::class
            "object" -> Map::class
            "uuid" -> UUID::class
            else -> {
                String::class //TODO - reference schemas
            }
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
            throw error("Paramter schema ${schema.title} is not a map schema, but is on an object param.")
        }
        return schema
            .properties //todo add additional properties as well
            .filter { it.value != null }
            .mapValues { ParamInfo(it.value, fullSpec, it.key, paramType) }
            .toMutableMap()

    }

    private fun deriveSchema(_schema: Schema<T>): Schema<Any> {
        return if (_schema.`$ref` == null || !_schema.`$ref`.contains("/")) {
            _schema as Schema<Any>
        } else {
            val schemaName = RefUtils.extractSimpleName(_schema.`$ref`).left as String
            fullSpec.components.schemas[schemaName] as Schema<Any>
        }
    }


    val isID = { name: String ->
        dataType == UUID::class
                || name.matches(Regex("^.*(Id|ID).*$"))
                || name.matches(Regex("^.*[-_](id)[-_].*$"))
                || name.matches(Regex("^(id)*[-_].*|.*[-_](id)*\$"))
    }

    fun addFailingValue(value: Any) {
        if (passingValues.contains(value)) return
        failingValues.add(value)
    }

//    fun addPassingValue(value: Any) {
//        passingValues.add(value)
//        failingValues.remove(value)
//    }
}

