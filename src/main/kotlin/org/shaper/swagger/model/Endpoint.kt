package org.shaper.swagger.model

import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class Endpoint(
    val method: HttpMethod,
    val url: String,
    val path: String,
    val swaggerUrlOrFile: String
)