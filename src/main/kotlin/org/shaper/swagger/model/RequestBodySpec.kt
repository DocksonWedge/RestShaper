package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody

class RequestBodySpec(private val requestBody: RequestBody?, private val fullSpec: OpenAPI) {

    //TODO support xml
    private val contentType = listOf("application/json", "*/*")
        .firstOrNull {
            requestBody?.content?.get(it)?.schema != null
        }
    private val jsonSchema = requestBody?.content?.get(contentType)?.schema
    private val jsonContentRef = jsonSchema?.`$ref` ?: ""
    private val schemaRef = RefUtils.extractSimpleName(jsonContentRef)

    val bodyInfo = getBody()

    fun hasBody(): Boolean {
        return bodyInfo != null
    }

    private fun getBody(): ParamInfo<Any>? {
        return if (jsonContentRef != "") {
            ParamInfo(fullSpec.components.schemas[schemaRef.left] as Schema<Any>, fullSpec)
        } else if (jsonSchema != null) {
            ParamInfo(jsonSchema, fullSpec)
        } else{
            null
        }
    }

}