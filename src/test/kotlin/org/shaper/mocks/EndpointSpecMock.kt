package org.shaper.mocks

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec

object EndpointSpecMock {
    fun getWithMockedSwagger(url: String, path: String, method: PathItem.HttpMethod) : EndpointSpec{
        val swaggerSpec = mockk<OpenAPI>()
        val swaggerOperation = mockk<Operation>()
        val swaggerParameter = mockk<Parameter>()
        every { swaggerSpec.servers[0].url } returns url
        every { swaggerSpec.paths[path]?.readOperationsMap()?.get(method) } returns swaggerOperation
        every { swaggerParameter.schema.type } returns "integer"
        every { swaggerParameter.`in` } returns "query"
        every { swaggerParameter.name } returns "someId"
        every { swaggerOperation.parameters } returns listOf(swaggerParameter)

        return EndpointSpec(swaggerSpec, PathItem.HttpMethod.GET, path)
    }
}