package org.shaper.global.kafka

import kotlinx.serialization.json.JsonPrimitive
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.serialization.StringSerializer
import org.shaper.generators.model.TestResult
import java.util.*


object ResultsProducer {

    private const val KAFKA_BROKER = "localhost:9092"
    private val producerReference = lazy { create() }

    private fun create(): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = KAFKA_BROKER
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    fun produceResultsFieldMessage(
        testResult: TestResult,
        fieldName: String,
        fullPath: String,
        title: String,
        value: JsonPrimitive
    ) {

    }

}