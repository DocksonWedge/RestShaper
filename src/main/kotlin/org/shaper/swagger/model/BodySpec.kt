package org.shaper.swagger.model

import io.swagger.v3.oas.models.parameters.RequestBody

class BodySpec(private val requestBody: RequestBody) {


    val body = mutableMapOf<String, ParameterSpec>()
}