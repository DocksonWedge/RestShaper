package org.shaper.generators.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ResponseData(
    val body: String,
    val statusCode: Int,
    val headers: Map<String, String>,
    val cookies: Map<String, String>
){
    val bodyParsed = if (body.isNotBlank()) Json.parseToJsonElement(body) else JsonPrimitive("")
}
