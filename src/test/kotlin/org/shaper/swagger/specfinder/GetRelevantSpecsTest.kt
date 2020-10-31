package org.shaper.swagger.specfinder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.SwaggerOperationNotFound
import kotlin.math.exp

class GetRelevantSpecsTest {
    private val exampleFolder = "src\\test\\Resources\\TestExamples"
    private val petStoreSwaggerLocation = "${exampleFolder}\\PetStoreSwagger.yaml"
    private val petStoreSwaggerLocationEdited = "${exampleFolder}\\PetStoreSwaggerEdited.yaml"

    @TestFactory
    fun `Test getRelevantSpecs returns correct total number of params`() = listOf(
        //Triple(petStoreSwaggerLocation, listOf(""), 0),  TODO lvl 1 - handle no endpoints as check all
        listOf("delete:/store/order/{orderId}") to 1,
        listOf("post:/pet/{petId}", "GET:/pet/{petId}", "delETE:/pet/{petId}") to 3,
        listOf("post:/pet", "post:/pet/{petId}") to 2
    )
        .map { (rawEndpoints, expected) ->
            DynamicTest.dynamicTest("when I retrieve '${rawEndpoints}' then I find ${expected} endpoints") {
                Assertions.assertEquals(
                    expected,
                    SpecFinder(petStoreSwaggerLocation, rawEndpoints).getRelevantSpecs().size
                )
            }
        }

    @TestFactory
    fun `Test getRelevantSpecs throws correct errors`() = listOf(
        listOf("post:/pet", "GET:/pet") to SwaggerOperationNotFound::class,
        listOf("") to IllegalArgumentException::class,
        listOf("post:/PET") to SwaggerOperationNotFound::class
    )
        .map { (rawEndpoints, expected) ->
            DynamicTest.dynamicTest("when I retrieve '${rawEndpoints}' then I find ${expected} error thrown") {
                Assertions.assertThrows(expected.javaObjectType) {
                    SpecFinder(petStoreSwaggerLocation, rawEndpoints).getRelevantSpecs()
                }
            }
        }


    @TestFactory
    fun `Test getRelevantSpecs returns params with the correct param data type, type, and name`() = listOf(
        listOf("get:/store/order/{orderId}", "delete:/store/order/{orderId}")
                to
                mapOf(
                    "size" to 1,
                    "names" to listOf("orderId"),
                    "paramType" to "path",
                    "dataTypes" to Long::class,
                    "isId" to true
                ),
        listOf("get:/user/login")
                to
                mapOf(
                    "size" to 2,
                    "names" to listOf("username", "password"),
                    "paramType" to "query",
                    "dataTypes" to String::class,
                    "isId" to false
                ),
        listOf("get:/user/logout", "get:/store/inventory")
                to
                mapOf(
                    "size" to 0,
                    "names" to listOf<String>(),
                    "paramType" to "",
                    "dataTypes" to Any::class,
                    "isId" to false
                ),
        //This is unique because it has form data the is NOT set in params right now
        listOf("post:/pet/{petId}")
                to
                mapOf(
                    "size" to 1,
                    "names" to listOf("petId"),
                    "paramType" to "path",
                    "dataTypes" to Long::class,
                    "isId" to true
                )

    )
        .map { (rawEndpoints, expected) ->
            DynamicTest.dynamicTest(
                "when I retrieve '${rawEndpoints}' " +
                        "then I find ${expected["names"]} parameter info, " +
                        "where types: $${expected["paramTypes"]}, " +
                        "dataTypes: $${expected["dataTypes"]}"
            ) {
                //actualSpecs calls swagger for spec, so only run once per test
                val actualSpecs = SpecFinder(
                    petStoreSwaggerLocation,
                    rawEndpoints
                ).getRelevantSpecs()

                Assertions.assertTrue(actualSpecs.all { it.params.entries.size == expected["size"] })
                Assertions.assertTrue(actualSpecs.all { it.params.keys.containsAll(expected["names"] as List<String>) })
                // TODO Should the last 3 be containsAll or check for all with AND
                Assertions.assertTrue(actualSpecs.all {
                    it.params.values.map { p -> p.paramType }
                        .all { pType -> pType == expected["paramType"] }
                })
                Assertions.assertTrue(actualSpecs.all {
                    it.params.values.map { p -> p.info.dataType }
                        .all { dType -> dType == expected["dataTypes"] }
                })
                Assertions.assertTrue(actualSpecs.all {
                    it.params.values.map { p -> p.info.isID(p.name) }
                        .all { isId -> isId == expected["isId"] }
                })

            }
        }

    @TestFactory
    fun `Test getRelevantSpecs returns params with the correct min max`() = listOf(
        petStoreSwaggerLocation
                to
                mapOf(
                    "max" to 10L,
                    "min" to 1L
                ),
        petStoreSwaggerLocationEdited
                to
                mapOf(
                    "max" to 100.0,
                    "min" to -1.0
                )

    )
        .map { (swaggerLocation, expected) ->
            DynamicTest.dynamicTest(
                "when I retrieve '$swaggerLocation' " +
                        "then I find ${expected["max"]} max value " +
                        "and I find ${expected["min"]} min value"
            ) {
                //actualSpecs calls swagger for spec, so only run once per test
                val actualSpecs = SpecFinder(
                    swaggerLocation,
                    listOf("get:/store/order/{orderId}")
                ).getRelevantSpecs()
                val param = actualSpecs[0].params.values.toList()[0].info
                when (param.dataType) {
                    Long::class -> {
                        Assertions.assertEquals(expected["max"], param.maxInt)
                        Assertions.assertEquals(expected["min"], param.minInt)
                    }
                    Double::class -> {
                        Assertions.assertEquals(expected["max"], param.maxDecimal)
                        Assertions.assertEquals(expected["min"], param.minDecimal)
                    }
                }
            }
        }

    @TestFactory
    fun `Test getRelevantSpecs returns params with the correct enum`() = listOf(
        listOf("get:/pet/findByStatus")
                to
                setOf("available", "pending", "sold")
    )
        .map { (rawEndpoints, expected) ->
            DynamicTest.dynamicTest(
                "when I retrieve '${rawEndpoints}' " +
                        "then I find $expected values in the passing values."
            ) {
                //actualSpecs calls swagger for spec, so only run once per test
                val actualSpecs = SpecFinder(
                    petStoreSwaggerLocation,
                    rawEndpoints
                ).getRelevantSpecs()
                val param = actualSpecs[0].params.values.toList()[0]
                Assertions.assertEquals(expected, param.info.passingValues)
            }
        }

    @TestFactory
    fun `Test getRelevantSpecs returns correct parameter location`() = listOf(
        listOf("delete:/pet/{petId}")
                to
                mapOf(
                    "api_key" to "header",
                    "petId" to "path"
                )

    )
        .map { (rawEndpoints, expected) ->
            DynamicTest.dynamicTest(
                "when I retrieve '${rawEndpoints}' " +
                        "then I find the expected parameter types: $expected"
            ) {
                //actualSpecs calls swagger for spec, so only run once per test
                SpecFinder(petStoreSwaggerLocation, rawEndpoints)
                    .getRelevantSpecs()[0]
                    .params
                    .forEach() {
                        Assertions.assertEquals(expected[it.key], it.value.paramType)
                    }
            }
        }
}
