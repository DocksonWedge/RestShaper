package org.shaper.swagger

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser

import org.shaper.swagger.model.EndpointSpec


class SpecFinder(
    private val urlOrFilePath: String,
    private val rawEndpoints: List<String> = listOf()
) {

    private val fullSpec = OpenAPIV3Parser().read(urlOrFilePath)
    //TODO lvl 1 - handle no endpoints as check all
    private val endpoints = rawEndpoints.map { endpointString ->
        endpointString.split(":").let { HttpMethod.valueOf(it[0].toUpperCase()) to it[1] }
    }

    //TODO make work with multiple specs
    fun getRelevantSpecs(): List<EndpointSpec> {
        return endpoints.mapNotNull { methodPathPair ->
            EndpointSpec(
                fullSpec.paths[methodPathPair.second]?.readOperationsMap()?.get(methodPathPair.first)
                    ?: throw SwaggerOperationNotFound(
                        "Could not find ${methodPathPair.second} ${methodPathPair.first} in swagger spec."
                    )
            )
        }
    }
}

class SwaggerOperationNotFound(message: String) : Exception(message)