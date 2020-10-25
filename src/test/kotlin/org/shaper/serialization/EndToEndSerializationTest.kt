package org.shaper.serialization

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.entry.dataAtWorkRun
import org.shaper.generators.model.TestResult
import org.shaper.global.results.ResultsStateGlobal

class EndToEndSerializationTest {

    @ResourceLock("ResultsStateGlobal")
    @Test
    fun `Test serializer goes to JSON and back to object`() {
        ResultsStateGlobal.clearResults()
        val resultsSerializer = ListSerializer(TestResult.serializer())
        dataAtWorkRun(1)
        val originalResults = ResultsStateGlobal.getAllResults()
        val resultsString =
            Json.encodeToString(
                resultsSerializer,
                ResultsStateGlobal.getAllResults()
            )
        val rebuiltObjects = Json.decodeFromString(resultsSerializer, resultsString)
        Assertions.assertEquals(2, rebuiltObjects.size)
        Assertions.assertTrue(rebuiltObjects[0].response.body.contains("{"))
        listOf("offset", "limit").forEach {
            Assertions.assertEquals(
                originalResults[0].input.queryParams[it].toString(),
                rebuiltObjects[0].input.queryParams[it]?.toString()
            )
        }
        Assertions.assertEquals(originalResults[0].endpoint, rebuiltObjects[0].endpoint)
        Assertions.assertEquals(originalResults[0].response.body, rebuiltObjects[0].response.body)
        Assertions.assertEquals(originalResults[0].response.headers, rebuiltObjects[0].response.headers)
    }
}