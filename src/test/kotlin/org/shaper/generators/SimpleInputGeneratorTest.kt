package org.shaper.generators

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.swagger.SpecFinder

class SimpleInputGeneratorTest {
    private val exampleFolder = "src\\test\\Resources\\TestExamples"
    private val petStoreSwaggerLocation = "${exampleFolder}\\PetStoreSwagger.yaml"


    @TestFactory
    fun `Test getInput returns ints correctly`() = listOf(
        null to 50,
        1 to 1,
        50 to 50, //TODO - sequence makes this take a long time - need to look into- actually, looks like it's not looping over the sequence that takes time
        17 to 17,
        0 to 0,
        -1 to 0
    )
        .map { (number, expected) ->
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
                val orderIdValues = input.pathParams["orderId"]
                Assertions.assertEquals(
                    expected,
                    orderIdValues!!.size
                )
                orderIdValues.forEach {
                    Assertions.assertDoesNotThrow { it as Long }
                }
                // count() uses the sequence to count the total, so it IS different than checking .size
                Assertions.assertEquals(
                    expected,
                    input.count()
                )
            }
        }


}