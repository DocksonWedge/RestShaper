package org.shaper.entry.model

import kotlinx.serialization.Serializable

@Serializable
data class DockerConfig(
    val numCases: Int,
    val swaggerLocation: String,
    val endpoints: List<SimpleEndpoint>
)