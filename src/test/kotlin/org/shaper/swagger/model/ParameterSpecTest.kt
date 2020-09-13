package org.shaper.swagger.model

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import org.junit.jupiter.api.*


class ParameterSpecTest {


    @TestFactory
    fun `Test getRelevantSpecs returns correct total number of params`() = listOf(
        Triple("order_id", "integer", true),
        Triple("id-pet", "integer", true),
        Triple("order_identity", "integer", false),
        Triple("mid-pet", "integer", false),
        Triple("my_ID", "integer", true),
        Triple("my_Id_of_doom", "integer", true),
        Triple("my_id-of_doom", "integer", true),
        Triple("my-ID-of_doom", "integer", true),
        Triple("random_num", "uuid", true),
        Triple("random_num", "string", false),
    )
        .map { (name, type, expected) ->
            val param = mockk<Parameter>()
            every { param.schema.type } returns type
            every { param.name } returns name
            every { param.`in` } returns "query"

            DynamicTest.dynamicTest("when I check '$name' with class $type then I find isID == $expected") {
                Assertions.assertEquals(
                    expected,
                    ParameterSpec(param).isID
                )
            }
        }
}