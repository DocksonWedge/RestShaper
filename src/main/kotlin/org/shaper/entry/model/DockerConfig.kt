package org.shaper.entry.model

import kotlinx.serialization.Serializable

@Serializable
data class DockerConfig(
    val numCases: Int = 1,
    val swaggerLocation: String,
    val endpoints: List<SimpleEndpoint>,
    val chainDepth: Int = 1
)