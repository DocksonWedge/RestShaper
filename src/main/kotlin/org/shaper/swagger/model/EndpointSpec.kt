package org.shaper.swagger.model

import com.google.common.collect.ImmutableMap
import io.swagger.models.Operation
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import java.util.*

class EndpointSpec(private val swaggerOperation: Operation)
{
    var queryParams = mutableMapOf<String, ParameterSpec>()
    var pathParams  = mutableMapOf<String, ParameterSpec>()
    var headers = mutableMapOf<String, ParameterSpec>()
    // could be a parameter spec if terminal
    // could be a nested list or map
    // TODO - for any requests that are just lists, wrap in a "data {[]}" map first
    var body = mutableMapOf<String, Any>()
    // TODO - test
    init{
        val allParams = swaggerOperation.parameters.toList()
        allParams.forEach {
            when(it.`in`){
                "query" -> addToQueryParams(it)
                "path"  -> addToPathParams(it)
            }
        }
        // we are done modifying, change to immutable
        queryParams = Collections.unmodifiableMap(queryParams)
        pathParams = Collections.unmodifiableMap(pathParams)
    }

    private fun addToQueryParams(param: Parameter){
        queryParams[param.name] = ParameterSpec((param as QueryParameter).type)
    }
    private fun addToPathParams(param: Parameter){}
}