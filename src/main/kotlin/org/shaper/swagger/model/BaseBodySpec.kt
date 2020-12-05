package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema

abstract class BaseBodySpec(protected val fullSpec: OpenAPI) {

    abstract val properties: Set<String>

    protected fun getFlatKeys(_schema: Schema<*>) : Set<String>{
        return getFlatKeys("", _schema) //TODO pass in name maybe?
            .filter { it.isNotBlank() }
            .map { it.toLowerCase() }
            .toSet()
    }

    private fun getFlatKeys(key: String, _schema: Schema<*>)
            : List<String> {
        val schema = getDirectSchema(_schema)
        return if (schema is ObjectSchema && schema.properties != null) {
            schema.properties.flatMap { property ->
                getFlatKeys(property.key, property.value)
                    .flatMap {
                        listOf(key.capitalize(), key.capitalize() + it)
                    }
            }
        } else if (schema is ArraySchema) {
            getFlatKeys("", schema.items)
                .flatMap {
                    listOf(key.capitalize(), key.capitalize() + it) // since "" the : is added already
                }
        } else {
            listOf(key.capitalize())
        }
    }

    protected fun getDirectSchema(schema: Schema<*>): Schema<*> {
        return if (!schema.`$ref`.isNullOrBlank()) {
            fullSpec.components.schemas[
                    RefUtils.extractSimpleName(schema.`$ref`).left as String
            ] as Schema<Any>

        } else {
            schema
        }
    }


}