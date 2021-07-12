package org.shaper.global.kafka

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.Endpoint
import java.util.*


object ResultsProducer {

    private const val KAFKA_BROKER = "localhost:9092"
    private val producer = lazy { create() }

    private fun create(): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = KAFKA_BROKER
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        return KafkaProducer<String, String>(props)
    }

    fun produceResultsFieldMessage(
        testResult: TestResult,
        fieldName: String,
        fullPath: String,
        title: String,
        value: JsonPrimitive
    ) {

        val messageObj = ResultValueMessage(
            testResult.input,
            testResult.endpoint,
            fieldName,
            fullPath,
            title,
            value
        )
        val message = Json.encodeToString(ResultValueMessage.serializer(), messageObj)
        // TODO create a test!
        producer.value.send(
            ProducerRecord(
                "result-store",
                "${testResult.endpoint.method}-${testResult.endpoint.path}-$fieldName",
                message
            )
        )
    }


    @Serializable
    private data class ResultValueMessage(
        val input: TestInputConcretion,
        val endpoint: Endpoint,
        val fieldName: String,
        val fullPath: String,
        val title: String,
        val value: JsonPrimitive
    )

}