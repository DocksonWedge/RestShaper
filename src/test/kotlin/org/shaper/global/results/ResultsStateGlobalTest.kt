package org.shaper.global.results

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.PathItem.HttpMethod
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.generators.model.TestInputConcretion
import org.shaper.generators.model.TestResult
import org.shaper.mocks.EndpointSpecMock


class ResultsStateGlobalTest {
    private val input = TestInputConcretion(
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        JsonObject(mapOf())
    )
    private val input2 = TestInputConcretion(
        mapOf<String, Any>("someId" to 4L),
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        JsonObject(mapOf())
    )
    private val input3 = TestInputConcretion(
        mapOf<String, Any>("someId" to -23L),
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        mapOf<String, Any>(),
        JsonObject(mapOf())
    )

    private val result1 = mockk<TestResult>()
    private val result2 = mockk<TestResult>()
    private val result3 = mockk<TestResult>()
    private val result4 = mockk<TestResult>()

    private val endpoint = EndpointSpecMock.getWithMockedSwagger("http://endpoint/", "path", HttpMethod.GET)

    @ResourceLock("ResultsStateGlobal")
    @Test
    fun `Test ResultsStateGlobal saves correctly`() {
        ResultsStateGlobal.clearResults()
        // test save on empty index
        ResultsStateGlobal.save(endpoint, 200, input, result1)
        val resultList1 =
            ResultsStateGlobal.index[endpoint.paramUrl()]?.get(endpoint.method)?.get(200)?.get(input.hashCode())
        Assertions.assertEquals(1, resultList1!!.size)
        Assertions.assertEquals(result1, resultList1[0])
        // test save on a previously created index
        ResultsStateGlobal.save(endpoint, 200, input, result2)
        val resultList2 =
            ResultsStateGlobal.index[endpoint.paramUrl()]?.get(endpoint.method)?.get(200)?.get(input.hashCode())
        Assertions.assertEquals(2, resultList2!!.size)
        Assertions.assertEquals(result1, resultList2[0])
        Assertions.assertEquals(result2, resultList2[1])
        // test save on a half created index
        ResultsStateGlobal.save(endpoint, 400, input, result2)
        val resultList3 =
            ResultsStateGlobal.index[endpoint.paramUrl()]?.get(endpoint.method)?.get(400)?.get(input.hashCode())

        Assertions.assertEquals(1, resultList3!!.size)
        Assertions.assertEquals(result2, resultList3[0])
        // check getIndexFromStatusCode gets the right number of values
        Assertions.assertEquals(1, ResultsStateGlobal
            .getIndexFromStatusCode(endpoint, 400)
            .flatMap { it.value }
            .size
        )
        Assertions.assertEquals(2,
            ResultsStateGlobal
                .getIndexFromStatusCode(endpoint, 200)
                .flatMap { it.value }
                .size
        )
        //check that we save fails correctly
        every { result3.response.statusCode } returns 500
        every { result3.input } returns input2
        Results.saveToGlobal(endpoint, sequenceOf(result3))
        Assertions.assertTrue{
            endpoint.params.all { it.value.info.failingValues.contains(4L) }
        }
        //check that we save fails correctly
        every { result4.response.statusCode } returns 200
        every { result4.input } returns input3
        Results.saveToGlobal(endpoint, sequenceOf(result4))
        Assertions.assertTrue{
            endpoint.params.all { it.value.info.passingValues.contains(-23L) }
        }
    }
}