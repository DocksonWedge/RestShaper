package org.shaper.generators.model

import io.restassured.response.Response
import org.shaper.swagger.model.EndpointSpec

data class TestResult(val result: Response, val input: TestInputConcretion, val endpoint: EndpointSpec)