package org.shaper.entry.docker


import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.shaper.entry.GeneralRun
import org.shaper.entry.model.DockerConfig
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    routing {
        post("/shaper/run") {
            val configString = call.receive<String>()
            val config = try {
                Json.decodeFromString(DockerConfig.serializer(), configString)
            } catch (e: SerializationException) {
                call.respondText(
                    "{ \"jsonProcessingError\": \"${e.message}\"}",
                    ContentType("application", "json"),
                    HttpStatusCode.BadRequest
                )
                null
            }

            if (config != null) {
                val runId = UUID.randomUUID().toString()
                Thread {
                    GeneralRun(
                        config.numCases,
                        config.swaggerLocation,
                        config.staticParams,
                        config.endpoints,
                        config.chainDepth,
                        runId
                    )
                }.start()
                call.respondText(
                    "{ \"runId\": \"$runId\" }",
                    ContentType("application", "json"),
                    HttpStatusCode.Accepted
                )
            }
        }
    }
}