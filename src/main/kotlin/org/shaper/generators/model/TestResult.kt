package org.shaper.generators.model

import io.restassured.response.Response
import kotlinx.serialization.Serializable
import org.joda.time.DateTime
import org.shaper.serialization.DateTimeSerializer
import org.shaper.serialization.ResponseSerializer
import org.shaper.swagger.model.Endpoint
import java.time.LocalDateTime

@Serializable
class TestResult(
    val response: ResponseData,
    val input: TestInputConcretion,
    val endpoint: Endpoint
) {
    @Serializable(with = DateTimeSerializer::class)
    val creationTime = DateTime.now()

    companion object{
        fun fromResponse(_response: Response): ResponseData {
            return ResponseData(
                _response.asString(),
                _response.statusCode,
                _response.headers.asList().map { it.name to it.value }.toMap(),
                _response.cookies
            )
        }
    }
    //add pass/fail field
}