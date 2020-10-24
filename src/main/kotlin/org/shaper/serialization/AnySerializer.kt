package org.shaper.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AnySerializer : KSerializer<Any?> {
    override val descriptor = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any?) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        //todo - make sure non-strings convert back to right types
        return decoder.decodeString()
    }
}