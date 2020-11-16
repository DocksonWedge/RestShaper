package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponses

class ResponseBodySpec(responses: ApiResponses, private val fullSpec: OpenAPI) {

    private val directSchemas = responses.map { response ->
        val contentType = listOf("application/json", "*/*")
            .firstOrNull {
                response.value?.content?.get(it)?.schema != null
            }
        response.value?.content?.get(contentType)?.schema
    }.toMutableList()

    private val jsonContentRefs = directSchemas.map { it?.`$ref` ?: "" }
    private val refs = jsonContentRefs.map { RefUtils.extractSimpleName(it).left as String }
    private val refProperties = refs.flatMap {
        if(fullSpec.components.schemas[it] is Schema) {
            getFlatKeys(
                "",
                fullSpec.components.schemas[it] as Schema<Any>
            )
        }else{
            listOf()
        }
    }

    private val directProperties = directSchemas.flatMap { schema ->
        getFlatKeys("", schema)
    }
    // { pet: { id: 16345 }} => petId : 16345
    //TODO - needs to handle sub-ref schemas
    val properties = refProperties.union(directProperties)

    private fun getFlatKeys(key: String, schema: Schema<*>?)
            : List<String> {
        return if (schema is ObjectSchema) {
            schema.properties.flatMap { property ->
                getFlatKeys(property.key, property.value)
                    .map {
                        key.capitalize() + it
                    }
            }
        } else {
            listOf(key.capitalize())
        }
    }
}