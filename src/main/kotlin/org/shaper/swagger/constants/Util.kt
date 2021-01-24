package org.shaper.swagger.constants

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.shaper.swagger.SwaggerOperationNotFound

object Util {

    fun getOperation(path: String, method: HttpMethod, swaggerSpec: OpenAPI): Operation {
        return swaggerSpec.paths[path]?.readOperationsMap()?.get(method)
            ?: throw SwaggerOperationNotFound("Could not find $method $path in swagger spec.")
    }
}