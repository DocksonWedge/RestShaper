package org.shaper.global.kafka


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.datetime.Instant
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.model.Endpoint
import java.util.*


object ResultsProducer {

    private const val KAFKA_BROKER = "localhost:9092"
    private val producer = lazy { create() }

    private fun create(): Producer<String, String> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = KAFKA_BROKER
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        return KafkaProducer<String, String>(props)
    }

    fun produceResultsFieldMessage(
        testResult: TestResult,
        fieldName: String,
        fullPath: String,
        title: String,
        value: JsonPrimitive
    ) {

        val valueMessageObj = ResultValueMessage(
            fieldName,
            fullPath,
            title,
            value,
            testResult.resultId
        )

        val valueMessage = Json.encodeToString(ResultValueMessage.serializer(), valueMessageObj)
        sendResultsMessage(valueMessage, "result-value-store", testResult, fieldName)

        val bodyMessageObj = ResultBodyMessage(
            testResult.input,
            testResult.endpoint,
            testResult.creationTime,
            testResult.response.statusCode,
            testResult.response.body,
            testResult.resultId
        )
        val bodyMessage = Json.encodeToString(ResultBodyMessage.serializer(), bodyMessageObj)
        sendResultsMessage(bodyMessage, "result-body-store", testResult, fieldName)
    }

    private fun sendResultsMessage(
        message: String,
        topic: String,
        testResult: TestResult,
        fieldName: String,
    ) {
        producer.value.send(
            ProducerRecord(
                topic,
                "${testResult.endpoint.method}-${testResult.endpoint.path}-$fieldName",
                message
            )
        )
    }


    // Value is the result for a particular field
    @Serializable
    private data class ResultValueMessage(
        val fieldName: String,
        val fullPath: String,
        val title: String,
        val value: JsonPrimitive?,
        val resultId: String
    )

    // Body is the OVERALL result
    @Serializable
    private data class ResultBodyMessage(
        val input: TestInputConcretion,
        val endpoint: Endpoint,
        val executionTime: Instant,
        val statusCode: Int,
        val responseBody: String,
        val resultId: String
    )

}