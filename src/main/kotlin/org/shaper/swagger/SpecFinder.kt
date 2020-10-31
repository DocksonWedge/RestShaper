package org.shaper.swagger

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser

import org.shaper.swagger.model.EndpointSpec


class SpecFinder(
    private val urlOrFilePath: String,
    private val rawEndpoints: List<String> = listOf(),
    private val formattedEndpoints: List<Pair<HttpMethod, String>> = listOf()
) {

    private val fullSpec = OpenAPIV3Parser().read(urlOrFilePath)

    //TODO lvl 1 - handle no endpoints as check all
    private val endpoints = formattedEndpoints.union(
        rawEndpoints.map { endpointString ->
            endpointString.split(":").let { HttpMethod.valueOf(it[0].toUpperCase()) to it[1] }
        }
    )

    fun getRelevantSpecs(): List<EndpointSpec> {
        return if (endpoints.isEmpty()) {
            // if no endpoints loop through all of them!
            fullSpec.paths.flatMap { path
                ->
                path.value.readOperationsMap()
                    .mapNotNull { operation ->
                        EndpointSpec(
                            fullSpec, operation.key, path.key,
                            swaggerUrlOrFile = urlOrFilePath
                        )
                    }
            }
        } else {
            //otherwise, get endpoints only for those provided.
            endpoints.mapNotNull { methodPathPair ->
                EndpointSpec(
                    fullSpec, methodPathPair.first, methodPathPair.second,
                    swaggerUrlOrFile = urlOrFilePath
                )
            }
        }
    }
}

class SwaggerOperationNotFound(message: String) : Exception(message)