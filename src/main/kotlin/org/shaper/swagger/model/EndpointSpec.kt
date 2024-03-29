package org.shaper.swagger.model

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.parameters.RequestBody
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult

import org.shaper.swagger.SwaggerOperationNotFound
import org.shaper.swagger.constants.Util
import org.shaper.tester.client.RestAssuredClient

class EndpointSpec(
    val swaggerSpec: OpenAPI,
    val method: HttpMethod,
    val path: String,
    // TODO can call function be private val?
    var callFunction: (EndpointSpec, TestInputConcretion, String) -> TestResult = RestAssuredClient.callRestAssured,
    private val swaggerUrlOrFile: String = ""
) {

    val swaggerOperation = Util.getOperation(path, method, swaggerSpec)

    val params = swaggerOperation.parameters?.map {
        it.name to ParameterSpec(it, swaggerSpec)
    }?.toMap() ?: mapOf()

    // could be a parameter spec if terminal
    // could be a nested list or map
    // TODO - for any requests that are just lists, wrap in a "data {[]}" map first?
    var requestBody = RequestBodySpec(swaggerOperation.requestBody ?: RequestBody(), swaggerSpec)

    val queryParams = params.filter { it.value.paramType == "query" }
    val pathParams = params.filter { it.value.paramType == "path" }
    val headerParams = deriveHeaderParams()
    val cookieParams = params.filter { it.value.paramType == "cookie" }

    val responseBody = ResponseBodySpec(swaggerOperation.responses, swaggerSpec)

    private val firstTag = swaggerOperation.tags?.firstOrNull() ?: ""
    private val pathTitle = findPathTitle(path)
    val title = if (pathTitle != "") pathTitle else firstTag

    companion object {
        internal fun findPathTitle(path: String): String {
            return path
                .split("/")
                .lastOrNull { !it.isPathVariable() && it.isNotBlank() }
                ?: ""
        }

        private fun String.isPathVariable(): Boolean {
            return this.matches(Regex("""^\{.*}${'$'}"""))
        }
    }

    //TODO figure out how to handle content type here - being overridden in RESTAssured call
    private fun deriveHeaderParams(): Map<String, ParameterSpec> {
        val headers = params.filter { it.value.paramType == "header" }
        return if (requestBody.hasBody()) {
            // add json application type if there is a body
            headers.plus("Content-Type" to ParameterSpec.getContentTypeParam(swaggerSpec))
        } else {
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
    val endpoint = Endpoint(method, url, path, swaggerUrlOrFile, title)

    fun callWithConcretion(input: TestInputConcretion, runId: String): TestResult {
        return callFunction(this, input, runId)
    }
}

