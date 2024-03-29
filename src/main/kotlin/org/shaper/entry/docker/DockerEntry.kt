package org.shaper.entry.docker

import kotlinx.serialization.json.Json
import org.shaper.entry.GeneralRun
import org.shaper.entry.model.DockerConfig
import java.io.File


fun main(vararg args: String) {
    val configLocation = args[0] //TODO this or environment variables?
    val config = Json.decodeFromString(
        DockerConfig.serializer(),
        File(configLocation).readText(Charsets.UTF_8)
    )
    GeneralRun(config.numCases, config.swaggerLocation, config.staticParams, config.endpoints, config.chainDepth)
}
