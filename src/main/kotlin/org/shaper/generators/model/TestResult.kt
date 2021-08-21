package org.shaper.generators.model

import io.restassured.internal.RestAssuredResponseImpl
import io.restassured.response.Response
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

import org.shaper.swagger.model.Endpoint
import java.util.*


@Serializable
data class TestResult(
    val response: ResponseData,
    val input: TestInputConcretion,
    val endpoint: Endpoint,
    val resultGroupId: String,
    val sourceResultIds: Set<String> = setOf<String>()
) {
    @Serializable
    val creationTime = Clock.System.now() //DateTime.now()

    @Serializable
    val resultId = UUID.randomUUID().toString()

    @Transient
    var restAssuredResponse: Response = RestAssuredResponseImpl()

    constructor(
        _response: Response,
        _input: TestInputConcretion,
        _endpoint: Endpoint,
        _resultGroupId: String,
        _sourceResultIds: Set<String> = setOf<String>()
    )
            : this(fromRestAssuredResponse(_response), _input, _endpoint, _resultGroupId, _sourceResultIds) {
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