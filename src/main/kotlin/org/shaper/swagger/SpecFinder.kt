package org.shaper.swagger

import io.ktor.client.HttpClient

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser



class SpecFinder(
    private val urlOrFilePath:String,
    private val rawEndpoints: List<String> = listOf()) {

    val endpoints = rawEndpoints.map { endpointString ->
        endpointString.split(":").let { it[0] to it[1] }
    }
    fun getRelevantSpec() : Swagger {
        return SwaggerParser().read(urlOrFilePath)
    }
}