package org.shaper.entry

import io.swagger.v3.oas.models.PathItem
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.config.runnerConfig
import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.StaticParams
import org.shaper.global.results.ResultsStateGlobal

// Hits external endpoint!

class SimpleRunnerTest {
    private val statics = StaticParams(
        headers = mapOf("api_key" to "token"),
        pathParams = mapOf("petId" to 12313213)
    )

    private fun petStoreGetPetStaticHeader(numCases: Int = 5, statics: StaticParams ): Boolean {
        return runnerConfig {
            inputFunction = SimpleInputGenerator(numCases)::getInput
            staticParams=statics
            endpointConfig = {
                swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
                endpoints = listOf(
                    PathItem.HttpMethod.DELETE to "/pet/{petId}"
                )
            }
        }.run()
    }

    @ResourceLock("ResultsStateGlobal")
    @Test
    fun `Test SimpleRunner runs a simple test!`() {
        ResultsStateGlobal.clearResults()
        val origSize = ResultsStateGlobal.getAllResults().size
        petStoreGetPet(4)
        Assertions.assertEquals(4, ResultsStateGlobal.getAllResults().size - origSize)
    }

    @ResourceLock("ResultsStateGlobal")
    @Test
    fun `Test SimpleRunner runs a test with a static header!`() {
        ResultsStateGlobal.clearResults()
        val origSize = ResultsStateGlobal.getAllResults().size
        petStoreGetPetStaticHeader(4, statics)
        Assertions.assertEquals(4, ResultsStateGlobal.getAllResults().size - origSize)
        Assertions.assertEquals(
            "token",
            ResultsStateGlobal.getAllResults()[0].input.headers["api_key"] as String
        )
        Assertions.assertEquals(
            12313213,
            ResultsStateGlobal.getAllResults()[0].input.pathParams["petId"] as Int
        )
    }

    @ResourceLock("ResultsStateGlobal")
    @Test
    fun `Test SimpleRunner runs a test with a random header!`() {
        ResultsStateGlobal.clearResults()
        val origSize = ResultsStateGlobal.getAllResults().size
        petStoreGetPetStaticHeader(4, StaticParams())
        Assertions.assertEquals(4, ResultsStateGlobal.getAllResults().size - origSize)
        Assertions.assertDoesNotThrow {
            ResultsStateGlobal.getAllResults()[0].input.headers["api_key"] as String
        }
    }
}