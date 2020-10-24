package org.shaper.generators.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseData(
    val body: String,
    val statusCode: Int,
    val Headers: Map<String, String>,
    val cookies: Map<String, String>
)