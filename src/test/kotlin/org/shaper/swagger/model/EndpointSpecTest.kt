package org.shaper.swagger.model

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.json.JsonObject
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.generators.model.TestInputConcretion
import org.shaper.mocks.EndpointSpecMock.getWithMockedSwagger

import java.util.*

class EndpointSpecTest {

    @TestFactory
    fun `Test fullUrl replaces path params correctly`() = listOf(
        Triple("/{orderId}", mapOf("orderId" to "myStringId"), "https://myUrl/myStringId"),
        Triple("/{orderid}", mapOf("orderId" to "myStringId"), "https://myUrl/{orderid}"),
        Triple("/{order Id}", mapOf("order Id" to 123L), "https://myUrl/123"),
        Triple(
            "/{order-ID}",
            mapOf("order-ID" to UUID.fromString("24ab6615-b3bc-453a-9029-bbf33f90d2be")),
            "https://myUrl/24ab6615-b3bc-453a-9029-bbf33f90d2be"
        ),
        Triple("/{order_Id}", mapOf("order_Id" to ""), "https://myUrl/"),
        Triple("/{orderId}", mapOf("randomparam" to "DNE"), "https://myUrl/{orderId}"),
        Triple("/{orderId}", mapOf("orderId" to null), "https://myUrl/null"),
        Triple(
            "/{orderId}/middle/{name}",
            mapOf("orderId" to 369L, "DNE" to "val", "name" to "param2"),
            "https://myUrl/369/middle/param2"
        ),
    )
        .map { (path, paramValues, expected) ->

            DynamicTest.dynamicTest(
                "when I get url for $path with values $paramValues " +
                        "then I expect the concrete URL of $expected"
            ) {

                val swaggerSpec = mockk<OpenAPI>()
                val swaggerOperation = mockk<Operation>()
                every { swaggerSpec.servers[0].url } returns "https://myUrl"
                every { swaggerSpec.paths[path]?.readOperationsMap()?.get(HttpMethod.POST) } returns swaggerOperation
                every { swaggerOperation.parameters } returns listOf()

                val endpoint = EndpointSpec(swaggerSpec, HttpMethod.POST, path)
                Assertions.assertEquals(
                    expected,
                    endpoint.fullUrl(paramValues)
                )

            }
        }

    // http://api.dataatwork.org/v1/spec/skills-api.json GET:/jobs
    @TestFactory //TODO how to call out that this requires an external resource?
    fun `Test callWithConcretion successfully calls an endpoint`() = listOf(
        "/jobs" to 4, //Expected is the number returned plus 1 for pagination info
        "/skills" to 4
    )
        .map { (path, expected) ->

            DynamicTest.dynamicTest(
                "when I CALL the url for $path with values then I expect a real response"
            ) {
                val endpoint = getWithMockedSwagger("http://api.dataatwork.org/v1", path, HttpMethod.GET)

                val input = TestInputConcretion(
                    mapOf("limit" to 3),
                    mapOf<String, Long>(),
                    mapOf<String, Long>(),
                    mapOf<String, Long>(),
                    JsonObject(mapOf())
                )
                //todo mock
                Assertions.assertDoesNotThrow {
                    endpoint.callWithConcretion(input).response.prettyPeek()
                        .then().assertThat()
                        .body("size()", equalTo(expected))
                }
            }
        }
}