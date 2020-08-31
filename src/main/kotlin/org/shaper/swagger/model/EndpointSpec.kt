package org.shaper.swagger.model


import io.swagger.v3.oas.models.Operation
import java.util.*

class EndpointSpec(private val swaggerOperation: Operation)
{
    val params = swaggerOperation.parameters?.map {
        it.name to ParameterSpec(it)
    }?.toMap() ?: mapOf()
    var headers = mutableMapOf<String, ParameterSpec>()
    var cookies = mutableMapOf<String, ParameterSpec>()
    // could be a parameter spec if terminal
    // could be a nested list or map
    // TODO - for any requests that are just lists, wrap in a "data {[]}" map first
    var body = mutableMapOf<String, Any>()

    val queryParams = params.filter { it.value.paramType == "query" }
    val pathParams = params.filter { it.value.paramType == "path" }
    val headerParams = params.filter { it.value.paramType == "header" }
    val cookieParams = params.filter { it.value.paramType == "cookie" }
}