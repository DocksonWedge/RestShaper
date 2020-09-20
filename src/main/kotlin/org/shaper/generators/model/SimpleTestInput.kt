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

        override fun hasNext(): Boolean {
            return (!hasStarted && !isEmpty())
                    ||
                    !(isOnLastParam(position.queryParams, queryParams)
                            && isOnLastParam(position.pathParams, pathParams)
                            && isOnLastParam(position.headers, headers)
                            && isOnLastParam(position.cookies, cookies))
        }

        private fun isEmpty(): Boolean {
            return hasNoValues(queryParams)
                    && hasNoValues(pathParams)
                    && hasNoValues(headers)
                    && hasNoValues(cookies)
        }

        private var hasStarted = false

        // TODO make abstract so util functions are reusable, next and hasNext implemented in child class
        override fun next(): TestInputConcretion {
            //don't skip 0th entry, is there a better way to iterate and also force one iteration on single runs?
            if (!hasStarted && !isEmpty()) {
                hasStarted = true
                return inputFromPosition(position)
            }
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