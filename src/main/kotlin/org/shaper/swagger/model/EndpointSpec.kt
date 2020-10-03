package org.shaper.swagger.model

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.OpenAPI
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult

import org.shaper.swagger.SwaggerOperationNotFound
import org.shaper.tester.client.RestAssuredClient


class EndpointSpec(
    private val swaggerSpec: OpenAPI,
    val method: HttpMethod,
    val path: String,
    var callFunction: (EndpointSpec, TestInputConcretion) -> TestResult = RestAssuredClient.callRestAssured
) {

    private val swaggerOperation = swaggerSpec.paths[path]?.readOperationsMap()?.get(method)
        ?: throw SwaggerOperationNotFound("Could not find ${method} ${path} in swagger spec.")

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

    // TODO support multiple servers
    // TODO support url parameters in the server
    val url = swaggerSpec.servers[0].url
    val fullUrl = { pathParams: Map<String, *> ->
        var pathConcretion = path
        pathParams.forEach { name, value ->
            pathConcretion = pathConcretion.replace("{$name}", value.toString(), false)
        }
        url + pathConcretion
    }

    fun callWithConcretion(input: TestInputConcretion): TestResult {
        return callFunction(this, input)
    }
}

