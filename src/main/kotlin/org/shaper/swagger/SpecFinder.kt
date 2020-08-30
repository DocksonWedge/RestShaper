package org.shaper.swagger



import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser

import org.shaper.swagger.model.EndpointSpec


class SpecFinder(
    private val urlOrFilePath: String,
    private val rawEndpoints: List<String> = listOf()
) {

    private val fullSpec = OpenAPIV3Parser().read(urlOrFilePath)
    private val endpoints = rawEndpoints.map { endpointString ->
        endpointString.split(":").let { HttpMethod.valueOf(it[0]) to it[1] }
    }

    //TODO make work with multiple specs
    fun getRelevantSpecs(): List<EndpointSpec> {
        return endpoints.mapNotNull { endpointString ->
            EndpointSpec(
                fullSpec.paths[endpointString.second]!!.readOperationsMap()[endpointString.first]
                    ?: throw SwaggerOperationNotFound(
                        "Could not find ${endpointString.second} ${endpointString.first} in swagger spec."
                    )
            )
        }
    }
}

class SwaggerOperationNotFound(message: String) : Exception(message)