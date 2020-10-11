package org.shaper.generators.model

import io.restassured.response.Response
import org.shaper.swagger.model.EndpointSpec
import java.time.Instant
import java.time.LocalDateTime

data class TestResult(val response: Response, val input: TestInputConcretion, val endpoint: EndpointSpec){
    val creationTime: LocalDateTime = LocalDateTime.now()
    //aadd pass/fail field
}