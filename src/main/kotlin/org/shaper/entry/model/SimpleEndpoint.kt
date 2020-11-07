package org.shaper.entry.model

import io.swagger.v3.oas.models.PathItem
import kotlinx.serialization.Serializable

@Serializable
data class SimpleEndpoint(val method: PathItem.HttpMethod, val path: String)