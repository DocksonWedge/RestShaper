package org.shaper.generators.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.shaper.serialization.AnySerializer

@Serializable
data class TestInputConcretion(
    //TODO make all test input strings!
    val queryParams: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val pathParams: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val headers: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val cookies: Map<String, @Serializable(with = AnySerializer::class) Any?>,
    val body: JsonObject
    ){
    fun requestBody(): String {
        return body.toString()
    }
}
