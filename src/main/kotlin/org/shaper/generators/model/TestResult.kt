package org.shaper.generators.model

import io.restassured.response.Response
import org.shaper.swagger.model.EndpointSpec
import java.time.Instant
import java.time.LocalDateTime

data class TestResult(val result: Response, val input: TestInputConcretion, val endpoint: EndpointSpec){
    val creationTime: LocalDateTime = LocalDateTime.now()
}