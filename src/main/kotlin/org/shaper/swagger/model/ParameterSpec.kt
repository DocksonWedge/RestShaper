package org.shaper.swagger.model


import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import java.math.BigDecimal

import java.util.*
import kotlin.reflect.KClass


class ParameterSpec(
    private val param: Parameter,
    private val fullSpec: OpenAPI
) {
    companion object {
        // make standard parameter for header content type
        fun getContentTypeParam(fullSpec: OpenAPI, contentType: String = "application/json"): ParameterSpec {
            val ctParam = Parameter()
            ctParam.name = "Content-Type"
            ctParam.`in` = "header"
            ctParam.schema = StringSchema()
            ctParam.schema.enum = listOf(contentType)
            return ParameterSpec(ctParam, fullSpec)
        }
    }

    val name: String = param.name
    val paramType: String = param.`in`
    val info = ParamInfo(param.schema, fullSpec, name, paramType)
}

