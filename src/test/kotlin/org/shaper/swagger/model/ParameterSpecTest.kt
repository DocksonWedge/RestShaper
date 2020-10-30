package org.shaper.swagger.model

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.util.*


class ParameterSpecTest {


    @TestFactory
    fun `Test ParameterSpec returns the correct data type`() = listOf(
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
            every { param.schema.maximum } returns null
            every { param.schema.minimum } returns BigDecimal(-1)
            every { param.schema.enum } returns null
            every { param.name } returns name
            every { param.`in` } returns "query"

            DynamicTest.dynamicTest("when I check '$name' with class $type then I find isID == $expected") {
                val spec = ParameterSpec(param)
                Assertions.assertEquals(expected, spec.isID)
                Assertions.assertEquals(-1L, spec.minInt)
                Assertions.assertEquals(-1.0, spec.minDecimal)
                Assertions.assertEquals(10000000000, spec.maxInt)
                Assertions.assertEquals(10000000000.0, spec.maxDecimal)
                when (type) {
                    "integer" -> Assertions.assertEquals(Long::class, spec.dataType)
                    "uuid" -> Assertions.assertEquals(UUID::class, spec.dataType)
                    "string" -> Assertions.assertEquals(String::class, spec.dataType)
                }
            }
        }
}