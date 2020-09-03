package org.shaper.swagger.specfinder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.SwaggerOperationNotFound

class GetRelevantSpecsTest {
    private val exampleFolder = "src\\test\\Resources\\TestExamples"
    val petStoreSwaggerLocation = "${exampleFolder}\\PetStoreSwagger.yaml"

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
                )
        ,
        listOf("get:/user/login")
                to
                mapOf(
                    "size" to 2,
                    "names" to listOf("username", "password"),
                    "paramType" to "query",
                    "dataTypes" to String::class,
                    "isId" to false
                )
        ,
        listOf("get:/user/logout", "get:/store/inventory")
                to
                mapOf(
                    "size" to 0,
                    "names" to listOf<String>(),
                    "paramType" to "",
                    "dataTypes" to Any::class,
                    "isId" to false
                )
        ,
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
                    it.params.values.map { p -> p.dataType }
                        .all { dType -> dType == expected["dataTypes"] }
                })
                Assertions.assertTrue(actualSpecs.all {
                    it.params.values.map { p -> p.isID }
                        .all { isId -> isId == expected["isId"] }
                })

            }
        }
}
