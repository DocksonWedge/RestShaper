package org.shaper.swagger.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.swagger.SpecFinder

class ResponseBodySpecTest {
    private val exampleFolder = "src\\test\\Resources\\TestExamples"
    private val petStoreSwaggerLocation = "${exampleFolder}\\PetStoreSwagger.yaml"

    @TestFactory
    fun `Test ResponseBodySpec has right properties`() = listOf(
        listOf("delete:/store/order/{orderId}") to setOf(),
        listOf("GET:/pet/{petId}") to setOf(
            "id",
            "category",
            "name",
            "photoUrls",
            "tags",
            "status",
            "category|~>id",
            "category|~>name",
            "tags|~>id",
            "tags|~>name"
        ),
        listOf("post:/store/order") to setOf(
            "id",
            "petId",
            "quantity",
            "shipDate",
            "status",
            "complete"
        ),
    )
        .map { (rawEndpoints, expected) ->
            DynamicTest.dynamicTest("when I retrieve '${rawEndpoints}' then I find ${expected} in response properties") {
                val actual = SpecFinder(petStoreSwaggerLocation, rawEndpoints)
                    .getRelevantSpecs()[0]
                    .responseBody
                    .properties
                Assertions.assertTrue(expected.containsAll(actual),
                    "$expected (expected) does not contain all $actual")
                Assertions.assertTrue(actual.containsAll(expected),
                    "$actual (actual) does not contain all $expected")
            }
        }

}