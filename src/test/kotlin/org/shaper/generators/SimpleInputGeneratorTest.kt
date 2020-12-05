package org.shaper.generators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.docksonwedge.kotmatcher.DockMatcher
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.shaper.generators.model.SimpleTestInput
import org.shaper.swagger.SpecFinder

class SimpleInputGeneratorTest {
    private val exampleFolder = "src\\test\\Resources\\TestExamples"
    private val petStoreSwaggerLocation = "${exampleFolder}\\PetStoreSwagger.yaml"


    @TestFactory
    fun `Test getInput returns ints correctly`() = listOf(
        null to 25,
        1 to 1,
        5 to 5,
        17 to 17,
        0 to 0,
        -1 to 0
    ).map { (number, expected) ->
        DynamicTest.dynamicTest(
            "when I retrieve '${number}' values from SimpleInputGenerator.getInput " +
                    "then I find ${expected} Long inputs are generated for orderId."
        ) {

            val endpoint =
                SpecFinder(petStoreSwaggerLocation, listOf("delete:/store/order/{orderId}"))
                    .getRelevantSpecs()[0]

            val input =
                (if (number == null) SimpleInputGenerator() else SimpleInputGenerator(number))
                    .getInput(endpoint) //TODO - this is the slow line

            Assertions.assertEquals(expected, input.toList().size)
            input.forEach {
                println("test run! ${it.pathParams["orderId"]}")
                Assertions.assertDoesNotThrow { it.pathParams["orderId"] as Long }
            }
            // count() uses the sequence to count the total, so it IS different than checking .size
            Assertions.assertEquals(expected, input.count())
        }
    }

    @TestFactory
    fun `Test getInput returns correct data type`() = listOf(
        Triple("get:/pet/findByStatus", List::class, {input: SimpleTestInput -> input.queryParams["status"] }),
        Triple("delete:/user/{username}", String::class, {input: SimpleTestInput -> input.pathParams["username"]}),
        Triple("Post:/pet", Map::class, {input: SimpleTestInput -> input.bodies}),
        Triple("POST:/user/createWithArray", List::class, {input: SimpleTestInput -> input.bodies}),
        Triple("delete:/pet/{petId}", String::class, {input: SimpleTestInput -> input.headers["api_key"]}),
    ).map { (endpoint, type, getParamValues) ->
        DynamicTest.dynamicTest(
            "when I retrieve '${endpoint}' values from SimpleInputGenerator.getInput " +
                    "then I find $type inputs are generated."
        ) {

            val endpointSpec =
                SpecFinder(petStoreSwaggerLocation, listOf(endpoint))
                    .getRelevantSpecs()[0]

            val input = SimpleInputGenerator().getInput(endpointSpec)
            val concreteValue = getParamValues(input)?.iterator()?.next()
            Assertions.assertTrue(type.isInstance(concreteValue),
                "Expected $type but value was ${concreteValue!!::class}")

        }
    }
    //TODO - why is this flaky? - due to mapOf() in RandomMapGenerator
    @Test
    fun `Test Categories field in request body is an object`() {
        val endpointSpec =
            SpecFinder(petStoreSwaggerLocation, listOf("Post:/pet"))
                .getRelevantSpecs()[0]

        val concreteValue = SimpleInputGenerator()
            .getInput(endpointSpec)
            .bodies
            .iterator().next() as JsonObject

        val category = concreteValue["category"] as JsonObject
        Assertions.assertTrue(JsonPrimitive::class.isInstance(category["name"]),
            "Expected String but value was ${category["name"]!!::class}")
        Assertions.assertTrue(JsonPrimitive::class.isInstance(category["id"]),
            "Expected Int but value was ${category["id"]!!::class}")

    }

}