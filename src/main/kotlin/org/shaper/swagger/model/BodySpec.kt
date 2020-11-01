package org.shaper.swagger.model

import io.swagger.v3.core.util.RefUtils
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody

class BodySpec(private val requestBody: RequestBody?, private val fullSpec: OpenAPI) {

    //TODO support xml
    val jsonContentRef = requestBody?.content?.get("application/json")?.schema?.`$ref` ?: ""
    private val schemaRef = RefUtils.extractSimpleName(jsonContentRef)
    // todo need function to not JUST use schema
    val bodyInfo = getBody()

    init {
        // See for how to link refs
        // https://github.com/swagger-api/swagger-core/blob/e260f6c3b811a920870aab3d11d59b3df1bee6f4/modules/swagger-core/src/main/java/io/swagger/v3/core/filter/SpecFilter.java#L436
        //schemaRef = RefUtils.extractSimpleName(jsonContentRef)
       // bodyInfo = ParamInfo(fullSpec.components.schemas[schemaRef.left] as Schema<Any>)
    }

    private fun getBody() : ParamInfo<Any>?{
        return if (jsonContentRef != "") {
            ParamInfo(fullSpec.components.schemas[schemaRef.left] as Schema<Any>)
        }else{
            null
        }
    }

}