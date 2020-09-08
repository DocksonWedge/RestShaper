package org.shaper.generators.model

import kotlinx.serialization.json.JsonObject

data class TestInputConcretion(
        val queryParams: Map<String,*>,
        val pathParams: Map<String,*>,
        val headers: Map<String,*>,
        val cookies: Map<String,*>,
        val body: JsonObject
    ){
    fun requestBody(): String {
        return body.toString()
    }
}
