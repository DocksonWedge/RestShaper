package org.shaper.swagger.model


import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import java.math.BigDecimal

import java.util.*
import kotlin.reflect.KClass


class ParameterSpec(
    private val param: Parameter
) {
    val name: String = param.name
    val paramType: String = param.`in`
    val info = ParamInfo(param.schema)
}

