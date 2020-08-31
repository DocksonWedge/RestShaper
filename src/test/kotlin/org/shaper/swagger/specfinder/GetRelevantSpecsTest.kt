package org.shaper.swagger.specfinder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.SwaggerOperationNotFound
import kotlin.reflect.KClass

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
                    "paramTypes" to listOf("path"),
                    "dataTypes" to listOf(Long::class)
                )
        ,
        listOf("get:/user/login")
                to
                mapOf(
                    "size" to 2,
                    "names" to listOf("username", "password"),
                    "paramTypes" to listOf("query"),
                    "dataTypes" to listOf(String::class)
                )
        ,
        listOf("get:/user/logout", "get:/store/inventory")
                to
                mapOf(
                    "size" to 0,
                    "names" to listOf<String>(),
                    "paramTypes" to listOf<String>(),
                    "dataTypes" to listOf<KClass<*>>()
                )
        ,
        //This is unique because it has form data the is NOT set in params right now
        listOf("post:/pet/{petId}")
                to
                mapOf(
                    "size" to 1,
                    "names" to listOf("petId"),
                    "paramTypes" to listOf("path"),
                    "dataTypes" to listOf(Long::class)
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
                Assertions.assertTrue(actualSpecs.all {
                    it.params.values.map { p -> p.paramType }.containsAll(expected["paramTypes"] as List<String>)
                })
                Assertions.assertTrue(actualSpecs.all {
                    it.params.values.map { p -> p.dataType }.containsAll(expected["dataTypes"] as List<KClass<*>>)
                })

            }
        }
}
