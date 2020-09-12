package org.shaper.generators.model

import kotlinx.serialization.json.JsonObject
import org.shaper.swagger.model.EndpointSpec
import org.shaper.swagger.model.ParameterSpec

class SimpleTestInput(
    queryParams: Map<String, List<*>>,
    pathParams: Map<String, List<*>>,
    headers: Map<String, List<*>>,
    cookies: Map<String, List<*>>,
    bodies: List<JsonObject>
) : BaseTestInput(queryParams, pathParams, headers, cookies, bodies) {

    override fun iterator(): Iterator<TestInputConcretion> {
        return SimpleTestInputIterator(
            queryParams,
            pathParams,
            headers,
            cookies,
            bodies
        )
    }

    class SimpleTestInputIterator(
        queryParams: Map<String, List<*>>,
        pathParams: Map<String, List<*>>,
        headers: Map<String, List<*>>,
        cookies: Map<String, List<*>>,
        bodies: List<JsonObject>
    ) : BaseTestInputIterator(queryParams, pathParams, headers, cookies, bodies) {

        var hasNext = true
        override fun hasNext(): Boolean {
            return !(isOnLastParam(position.queryParams, queryParams)
                    && isOnLastParam(position.pathParams, pathParams)
                    && isOnLastParam(position.headers, headers)
                    && isOnLastParam(position.cookies, cookies))
        }

        // TODO make abstract so util functions are reusable, next and hasNext implemented in child class
        override fun next(): TestInputConcretion {
            val previousPosition = position.copy()
            position.queryParams = nextParam(queryParams, position.queryParams)
            if (!isParamReset(
                    position.queryParams,
                    previousPosition.queryParams,
                    queryParams
                )
            ) return inputFromPosition(position)

            position.pathParams = nextParam(pathParams, position.pathParams)
            if (!isParamReset(
                    position.pathParams,
                    previousPosition.pathParams,
                    pathParams
                )
            ) return inputFromPosition(position)

            position.headers = nextParam(headers, position.headers)
            if (!isParamReset(
                    position.headers,
                    previousPosition.headers,
                    headers
                )
            ) return inputFromPosition(position)

            position.cookies = nextParam(cookies, position.cookies)
//            if (!isParamReset(position.cookies, previousPosition.cookies, cookies)) hasNext = false
            return inputFromPosition(position)
        }

    }


}