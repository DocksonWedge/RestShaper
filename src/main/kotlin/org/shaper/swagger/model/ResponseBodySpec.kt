package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponses

class ResponseBodySpec(responses: ApiResponses?, private val fullSpec: OpenAPI) {

    private val jsonSchemas = responses?.mapNotNull { response ->
        val contentType = listOf("application/json", "*/*")
            .firstOrNull {
                response.value?.content?.get(it)?.schema != null
            }
        if (contentType != null) {
            response.value?.content?.get(contentType)?.schema
        } else {
            null
        }
    } ?: listOf()

    // { pet: { id: 16345 }} => PetId : 16345
    val properties = jsonSchemas //todo remove since on the lower level?
        .flatMap { schema ->
            getFlatKeys("", schema) // extract the property names
        }
        .filter { it.isNotBlank() } //if only "" exists remove it
        .map { if (it.subSequence(0,1) == ":") it.substring(1) else it  } //remove leading :

    private fun getFlatKeys(key: String, _schema: Schema<*>)
            : List<String> {
        val schema = getDirectSchema(_schema)
        return if (schema is ObjectSchema && schema.properties != null) {
            schema.properties.flatMap { property ->
                getFlatKeys(property.key, property.value)
                    .flatMap {
                        listOf(key.capitalize(), key.capitalize() + ":" + it)
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

    private fun getDirectSchemas(schemas: List<Schema<*>>): Set<Schema<*>> {
        return schemas
            .filter { !it.`$ref`.isNullOrBlank() }
            .map { RefUtils.extractSimpleName(it.`$ref`).left as String }
            .flatMap { getDirectSchemas(listOf(fullSpec.components.schemas[it] as Schema<Any>)) }
            .union(
                schemas.filter { it.`$ref`.isNullOrBlank() } //direct non-ref schemas
            )
    }

    private fun getDirectSchema(schema: Schema<*>): Schema<*> {
        return if (!schema.`$ref`.isNullOrBlank()) {
            fullSpec.components.schemas[
                    RefUtils.extractSimpleName(schema.`$ref`).left as String
            ] as Schema<Any>

        } else {
            schema
        }
    }
}