package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponses

class ResponseBodySpec(responses: ApiResponses?, fullSpec: OpenAPI) : BaseBodySpec(fullSpec) {

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
    override val properties = jsonSchemas //todo remove since on the lower level?
        .flatMap { schema ->
            getFlatKeys(schema) // extract the property names
        }.toSet()
}