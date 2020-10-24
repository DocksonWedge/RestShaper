package org.shaper.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.time.format.FormatStyle
import java.util.*

object DateTimeSerializer : KSerializer<DateTime> {
    private val formatter = DateTimeFormat.fullDateTime()
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(formatter.print(value))
    }

    override fun deserialize(decoder: Decoder): DateTime {
        return formatter.parseDateTime(decoder.decodeString())
    }
}
