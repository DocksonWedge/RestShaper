package org.shaper.serialization

import io.restassured.internal.RestAssuredResponseImpl
import io.restassured.response.Response
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object ResponseSerializer : KSerializer<Response> {
    override val descriptor = PrimitiveSerialDescriptor("Response", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Response {
        val responseJson = Json.parseToJsonElement(decoder.decodeString()).jsonObject
        val response = RestAssuredResponseImpl()
        //response.statusCode =  responseJson["statusCode"]?.toString() ?: ""
        return response
    }

    override fun serialize(encoder: Encoder, value: Response) {
        val responseJson =
            buildJsonObject {
                put("body", value.asString())
                put("statusCode", value.statusCode())
                putJsonObject("cookies") {
                    value.cookies.forEach {
                        put(it.key, it.value)
                    }
                }
                putJsonObject("headers") {
                    value.headers.forEach {
                        put(it.name, it.value)
                    }
                }
            }
        val x = responseJson.toString()
        encoder.encodeString(responseJson.toString())
    }
}
