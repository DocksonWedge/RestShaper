package org.shaper.generators.model

import kotlinx.serialization.Serializable
import org.shaper.serialization.AnySerializer

data class StaticParams(
    val queryParams: Map<String, @Serializable(with = AnySerializer::class) Any?> = mapOf(),
    val pathParams: Map<String, @Serializable(with = AnySerializer::class) Any?> = mapOf(),
    val headers: Map<String, @Serializable(with = AnySerializer::class) Any?> = mapOf(),
    val cookies: Map<String, @Serializable(with = AnySerializer::class) Any?> = mapOf(),
)