package org.shaper.generators.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import org.shaper.serialization.AnySerializer

@Serializable
data class TestInputConcretion(
    //TODO make all test input strings!
    val queryParams: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val pathParams: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val headers: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val cookies: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val body: JsonElement
) {
    fun requestBody(): String {
        return body.toString()
    }
    @Transient
    val sourceResultIds = mutableSetOf<String>()
}
