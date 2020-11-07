package org.shaper.swagger.model

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.parameters.RequestBody
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult

import org.shaper.swagger.SwaggerOperationNotFound
import org.shaper.tester.client.RestAssuredClient

class EndpointSpec(
    private val swaggerSpec: OpenAPI,
    val method: HttpMethod,
    val path: String,
    // TODO can call function be private val?
    var callFunction: (EndpointSpec, TestInputConcretion) -> TestResult = RestAssuredClient.callRestAssured,
    private val swaggerUrlOrFile: String = ""
) {

    private val swaggerOperation = swaggerSpec.paths[path]?.readOperationsMap()?.get(method)
        ?: throw SwaggerOperationNotFound("Could not find ${method} ${path} in swagger spec.")

    val params = swaggerOperation.parameters?.map {
        it.name to ParameterSpec(it, swaggerSpec)
    }?.toMap() ?: mapOf()

    // could be a parameter spec if terminal
    // could be a nested list or map
    // TODO - for any requests that are just lists, wrap in a "data {[]}" map first?
    var body = BodySpec(swaggerOperation.requestBody ?: RequestBody(), swaggerSpec)

    val queryParams = params.filter { it.value.paramType == "query" }
    val pathParams = params.filter { it.value.paramType == "path" }
    val headerParams = deriveHeaderParams()
    val cookieParams = params.filter { it.value.paramType == "cookie" }

    //TODO figure out how to handle contenttype here - being overriden in RESTAssured call
    private fun deriveHeaderParams() : Map<String, ParameterSpec> {
        val headers = params.filter { it.value.paramType == "header" }
        return if (body.hasBody()) {
            // add json application type if there is a body
            headers.plus("Content-Type" to ParameterSpec.getContentTypeParam(swaggerSpec))
        }else{
            headers
        }

    }

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
    val paramUrl = {
        url + path
    }
    val endpoint = Endpoint(method, url, path, swaggerUrlOrFile)

    fun callWithConcretion(input: TestInputConcretion): TestResult {
        return callFunction(this, input)
    }
}

