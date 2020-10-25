package org.shaper.generators.model

import io.restassured.internal.RestAssuredResponseImpl
import io.restassured.response.Response
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.joda.time.DateTime
import org.shaper.serialization.DateTimeSerializer
import org.shaper.swagger.model.Endpoint


@Serializable
data class TestResult(
    val response: ResponseData,
    val input: TestInputConcretion,
    val endpoint: Endpoint

) {
    @Serializable(with = DateTimeSerializer::class)
    val creationTime = DateTime.now()

    @Transient
    var restAssuredResponse: Response = RestAssuredResponseImpl()

    constructor(_response: Response, _input: TestInputConcretion, _endpoint: Endpoint)
            : this(fromRestAssuredResponse(_response), _input, _endpoint) {
        restAssuredResponse = _response
    }

    companion object {
        fun fromRestAssuredResponse(_response: Response): ResponseData {
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