package org.shaper.tester.client

import io.restassured.RestAssured
import mu.KotlinLogging
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.EndpointSpec

object RestAssuredClient {
    private val logger = KotlinLogging.logger {}

    val callRestAssured = { endpointSpec: EndpointSpec, input: TestInputConcretion ->
        logger.info { "Calling ${endpointSpec.method} => ${endpointSpec.fullUrl(input.pathParams)}" }
        logger.info { " query params: ${input.queryParams}" }
        logger.info { " header params: ${input.headers}" }
        logger.info { " body: ${input.requestBody()}" }
        val result = RestAssured.given()
            .queryParams(input.queryParams)
            .headers(input.headers)
            .header("Content-Type", "application/json")
            .body(input.requestBody())
            .request(endpointSpec.method.toString(), endpointSpec.fullUrl(input.pathParams))
            ?: throw error("Calling ${endpointSpec.method} ${endpointSpec.url} with input $input failed to return a request!")
        result.prettyPeek()
        TestResult(result, input, endpointSpec.endpoint)
    }
}