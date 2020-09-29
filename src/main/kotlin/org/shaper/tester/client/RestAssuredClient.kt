package org.shaper.tester.client

import io.restassured.RestAssured
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec

object RestAssuredClient {
    val callRestAssured = { endpoint: EndpointSpec, input: TestInputConcretion ->
        val result = RestAssured.given()
            .queryParams(input.queryParams)
            .request(endpoint.method.toString(), endpoint.fullUrl(input.pathParams))
            ?: throw error("Calling ${endpoint.method} ${endpoint.url} with input $input failed to return a request!")
        TestResult(result, input, endpoint)
    }
}