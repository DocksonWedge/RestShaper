package org.shaper.tester.client

import io.restassured.RestAssured
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec

object RestAssuredClient {
    val callRestAssured = { endpointSpec: EndpointSpec, input: TestInputConcretion ->
        val result = RestAssured.given()
            .queryParams(input.queryParams)
            .body(input.requestBody())
            .request(endpointSpec.method.toString(), endpointSpec.fullUrl(input.pathParams))
            ?: throw error("Calling ${endpointSpec.method} ${endpointSpec.url} with input $input failed to return a request!")
        TestResult(result, input, endpointSpec.endpoint)
    }
}