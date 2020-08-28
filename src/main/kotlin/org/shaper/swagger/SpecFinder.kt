package org.shaper.swagger

import io.ktor.client.HttpClient
import io.swagger.models.HttpMethod
import io.swagger.models.Operation

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.shaper.swagger.model.EndpointSpec


class SpecFinder(
    private val urlOrFilePath: String,
    private val rawEndpoints: List<String> = listOf()
) {

    private val fullSpec = SwaggerParser().read(urlOrFilePath)
    private val endpoints = rawEndpoints.map { endpointString ->
        endpointString.split(":").let { HttpMethod.valueOf(it[0]) to it[1] }
    }

    //TODO make work with multiple specs
    fun getRelevantSpecs(): List<EndpointSpec> {
        return endpoints.mapNotNull { endpointString ->
            EndpointSpec(
                fullSpec.getPath(endpointString.second).operationMap[endpointString.first]
                    ?: throw SwaggerOperationNotFound(
                        "Could not find ${endpointString.second} ${endpointString.first} in swagger spec."
                    )
            )
        }
    }
}

class SwaggerOperationNotFound(message: String) : Exception(message)