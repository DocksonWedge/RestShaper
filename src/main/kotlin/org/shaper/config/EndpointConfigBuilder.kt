package org.shaper.config

import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.model.EndpointSpec

class EndpointConfigBuilder {
    var swaggerUrl = ""
    var endpoints = listOf<Pair<HttpMethod, String>>()

    fun build(): Sequence<EndpointSpec> {
        return SpecFinder(
            urlOrFilePath = swaggerUrl,
            formattedEndpoints = endpoints
        )
            .getRelevantSpecs()
            .asSequence()
    }
}