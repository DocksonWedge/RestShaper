package org.shaper.generators.model

import kotlinx.serialization.json.JsonObject

class SimpleTestInput(
    queryParams: Map<String, List<*>>, //TODO - this should take a sequence instead of list!
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
                    !(isOnLastParam(position.queryParams)
                            && isOnLastParam(position.pathParams)
                            && isOnLastParam(position.headers)
                            && isOnLastParam(position.cookies))
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
            nextParam(position.queryParams, queryParams)
            if (!isParamReset(position.queryParams)) return inputFromPosition(position)

            nextParam(position.pathParams, pathParams)
            if (!isParamReset(position.pathParams)) return inputFromPosition(position)

            nextParam(position.headers, headers)
            if (!isParamReset(position.headers)) return inputFromPosition(position)

            nextParam(position.cookies, cookies)
//            if (!isParamReset(position.cookies, previousPosition.cookies, cookies)) hasNext = false
            return inputFromPosition(position)
        }

    }


}