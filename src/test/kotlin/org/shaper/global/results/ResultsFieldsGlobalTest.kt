package org.shaper.global.results

import io.mockk.mockk
import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.common.Urls
import org.shaper.config.EndpointConfigBuilder
import org.shaper.config.endpointConfig
import org.shaper.entry.*
import org.shaper.generators.model.ResponseData
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.swagger.constants.Util
import org.shaper.swagger.model.ResponseBodySpec

private val logger = KotlinLogging.logger {}

class ResultsFieldsGlobalTest {

    val orderBody = buildJsonObject {
        put("id", 123)
        put("petId", 12)
        put("quantity", 0)
        put("shipDate", "2021-01-23T17:07:54.736Z")
        put("status", "placed")
        put("complete", true)
    }

    val petBody = buildJsonObject {
        put("id", 12345)
        putJsonObject("category") {
            put("id", 7)
            put("name", "category thing")
        }
        put("name", "fido")
        putJsonArray("photoUrls") {
            add("abc")
            add("123")
        }
        putJsonArray("tags") {
            addJsonObject {
                put("id", 1)
                put("name", "tag1")
            }
            addJsonObject {
                put("id", 2)
                put("name", "tag2")
            }
        }
        put("status", "available")
    }

    @TestFactory
    @ResourceLock("ResultsStateGlobal")
    fun `Test flat results fields gets saved in normal index`() = listOf(
        testInput(HttpMethod.POST, "/store/order", orderBody, this::assertOrder),
        testInput(HttpMethod.GET, "/pet/{petId}", petBody, this::assertPet),
    ).map { (method, path, body, assertion) ->
        DynamicTest.dynamicTest(
            "when I save results from $method $path " +
                    "then I find properties $body in the ResultsFieldsGlobal.index."
        ) {
            testGlobalSave(method, path, body)
            logger.info { ResultsFieldsGlobal.getIndex() }
            assertion()
        }
    }

    //***
    // Assertion helpers
    //***
    private fun assertOrder() {
        assertOrderField("id")
        assertOrderField("petId")
        assertOrderField("quantity")
        assertOrderField("shipDate")
        assertOrderField("status")
        assertOrderField("complete")
    }

    private fun assertPet() {
        Assertions.assertEquals(4, ResultsFieldsGlobal.getIndex()["id"]?.size)
        Assertions.assertEquals(4, ResultsFieldsGlobal.getIndex()["name"]?.size)
        assertField("categoryname", petBody["category"]?.jsonObject?.get("name"))
        assertField("categoryid", petBody["category"]?.jsonObject?.get("id"))
        Assertions.assertEquals(2, ResultsFieldsGlobal.getIndex()["photourls"]?.size)
        assertList("photourls", setOf(JsonPrimitive("abc"), JsonPrimitive("123")))
        assertList("tagsid", setOf(JsonPrimitive(1), JsonPrimitive(2)))
        assertList("tagsname", setOf(JsonPrimitive("tag1"), JsonPrimitive("tag2")))
        assertField("status", petBody["status"])
    }

    private fun assertOrderField(fieldName: String) {
        assertField(fieldName, orderBody[fieldName])
    }

    private fun assertField(indexPath: String, expectedValue: JsonElement?) {
        val valueSourcePair = ResultsFieldsGlobal.getIndex()[indexPath.toLowerCase()]!!.first()
        Assertions.assertEquals(expectedValue, valueSourcePair.first)
        Assertions.assertEquals(36,valueSourcePair.second.length)
    }

    private fun assertList(indexPath: String, set: Set<JsonPrimitive>) {
        Assertions.assertTrue(ResultsFieldsGlobal.getIndex()[indexPath]!!
            .map { it.first }
            .containsAll(set))
    }

    //***
    // test run impls
    //***
    private fun testGlobalSave(method: HttpMethod, path: String, body: JsonObject) {
        val responseData = ResponseData(body.toString(), 201, mapOf(), mapOf())
        val endpointSpec = endpointConfig {
            swaggerUrl = Urls.petStoreSwaggerLocation
            endpoints = listOf(method to path)
        }.build().first()

        val swaggerOperation = Util.getOperation(path, method, endpointSpec.swaggerSpec)
        val responseBody = ResponseBodySpec(endpointSpec.swaggerOperation.responses, endpointSpec.swaggerSpec)
        ResultsFieldsGlobal.initGlobals(reset = true)
        val result = TestResult(
            responseData,
            TestInputConcretion(mapOf(), mapOf(), mapOf(), mapOf(), JsonPrimitive("")),
            endpointSpec.endpoint,
            ""
        )
        ResultsFieldsGlobal.save(result)
    }

    private data class testInput(
        val method: HttpMethod,
        val path: String,
        val body: JsonObject,
        val assertion: () -> Unit
    )

}