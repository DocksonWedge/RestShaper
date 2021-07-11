package org.shaper.mocks

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import org.shaper.swagger.constants.Util
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec
import java.math.BigDecimal

object EndpointSpecMock {
    fun getWithMockedSwagger(mockUrl: String, path: String, method: PathItem.HttpMethod) : EndpointSpec{
        val swaggerSpec = mockk<OpenAPI>()
        val swaggerOperation = mockk<Operation>()
        val swaggerParameter = mockk<Parameter>()
        mockkObject(Util)
        every { swaggerSpec.servers } returns listOf(
            mockk {
                every{ url } returns mockUrl
            }
        )
        every { Util.getOperation(path, method, swaggerSpec) } returns swaggerOperation
        every { swaggerParameter.schema.type } returns "integer"
        every { swaggerParameter.schema.`$ref` } returns ""
        every { swaggerParameter.schema.maximum } returns BigDecimal(-1.2351)
        every { swaggerParameter.schema.minimum } returns null
        every { swaggerParameter.schema.enum } returns null
        every { swaggerParameter.`in` } returns "query"
        every { swaggerParameter.name } returns "someId"
        every { swaggerOperation.parameters } returns listOf(swaggerParameter)
        every { swaggerOperation.requestBody } returns null
        every { swaggerOperation.responses } returns null
        every { swaggerOperation.tags } returns null

        return EndpointSpec(swaggerSpec, PathItem.HttpMethod.GET, path)
    }
}