package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import org.apache.commons.lang3.tuple.Pair

class BodySpec(private val requestBody: RequestBody?, private val fullSpec: OpenAPI) {

    //TODO support xml
    val jsonContentRef = requestBody?.content?.get("application/json")?.schema?.`$ref` ?: ""
    lateinit var schemaRef: Pair<Any, Any>
    lateinit var body: ParamInfo<Any>
    init {
        if (jsonContentRef.contains("components/schemas")) {
            schemaRef = RefUtils.extractSimpleName(jsonContentRef)
            body = ParamInfo(fullSpec.components.schemas[schemaRef.left] as Schema<Any>)
            //throw NotImplementedError("Found json body with unsupported type in swagger spec: $schemaRef")
        }
    }

}