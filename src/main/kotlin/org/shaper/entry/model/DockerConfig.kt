package org.shaper.entry.model

import kotlinx.serialization.Serializable
import org.shaper.generators.model.StaticParams

@Serializable
data class DockerConfig(
    val numCases: Int = 1,
    val swaggerLocation: String,
    val staticParams: StaticParams = StaticParams(),
    val endpoints: List<SimpleEndpoint>,
    val chainDepth: Int = 1
)