package org.shaper.generators.model


class SimpleTestInput(
    queryParams: Map<String, Sequence<*>>, //TODO - this should take a sequence instead of list!
    pathParams: Map<String, Sequence<*>>,
    headers: Map<String, Sequence<*>>,
    cookies: Map<String, Sequence<*>>,
    bodies: Sequence<*>,
    private val numCases: Int = 25,
    sourceIdMap: SourceIdMap
) : BaseTestInput(queryParams, pathParams, headers, cookies, bodies, sourceIdMap) {

    override fun iterator(): Iterator<TestInputConcretion> {
        return SimpleTestInputIterator(
            queryParams,
            pathParams,
            headers,
            cookies,
            bodies,
            sourceIdMap,
            numCases
        )
    }

    class SimpleTestInputIterator(
        queryParams: Map<String, Sequence<*>>,
        pathParams: Map<String, Sequence<*>>,
        headers: Map<String, Sequence<*>>,
        cookies: Map<String, Sequence<*>>,
        bodies: Sequence<*>,
        sourceIdMap: SourceIdMap,
        private val numCases: Int
    ) : BaseTestInputIterator(queryParams, pathParams, headers, cookies, bodies, sourceIdMap) {
        private var i = 0
        override fun hasNext(): Boolean {
            return i < numCases
        }

        // TODO make abstract so util functions are reusable, next and hasNext implemented in child class
        override fun next(): TestInputConcretion {
            i++
            position.queryParams.forEach { it.value.next() }
            position.pathParams.forEach { it.value.next() }
            position.headers.forEach { it.value.next() }
            position.cookies.forEach { it.value.next() }
            return inputFromPosition(position, sourceIdMap)
        }

    }


}