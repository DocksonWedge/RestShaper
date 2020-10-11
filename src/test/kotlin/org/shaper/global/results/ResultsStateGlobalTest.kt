package org.shaper.global.results

import io.mockk.mockk
import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.shaper.generators.model.SimpleTestInput
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.mocks.EndpointSpecMock


class ResultsStateGlobalTest {

    @Test
    fun `Test ResultsStateGlobal saves correctly`() {
        //setup
        val input = TestInputConcretion(
            mapOf<String, Any>(),
            mapOf<String, Any>(),
            mapOf<String, Any>(),
            mapOf<String, Any>(),
            JsonObject(mapOf())
        )
        val result1 = mockk<TestResult>()
        val result2 = mockk<TestResult>()

        val endpoint = EndpointSpecMock.getWithMockedSwagger("http://endpoint/", "path", HttpMethod.GET)
        // test save on empty index
        ResultsStateGlobal.saveToGlobal(endpoint, input, 200, result1)
        val resultList1 = ResultsStateGlobal.index[endpoint.paramUrl()]?.get(endpoint.method)?.get(input.hashCode())?.get(200)
        Assertions.assertEquals(1, resultList1!!.size)
        Assertions.assertEquals(result1, resultList1[0])
        // test save on a previously created index
        ResultsStateGlobal.saveToGlobal(endpoint, input, 200, result2)
        val resultList2 = ResultsStateGlobal.index[endpoint.paramUrl()]?.get(endpoint.method)?.get(input.hashCode())?.get(200)
        Assertions.assertEquals(2, resultList2!!.size)
        Assertions.assertEquals(result1, resultList2[0])
        Assertions.assertEquals(result2, resultList2[1])
        // test save on a half created index
        ResultsStateGlobal.saveToGlobal(endpoint, input, 400, result2)
        val resultList3 = ResultsStateGlobal.index[endpoint.paramUrl()]?.get(endpoint.method)?.get(input.hashCode())?.get(400)
        Assertions.assertEquals(1, resultList3!!.size)
        Assertions.assertEquals(result2, resultList3[0])
    }
}