package org.shaper.swagger.model

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

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
}