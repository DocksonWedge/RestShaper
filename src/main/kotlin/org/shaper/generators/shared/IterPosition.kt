package org.shaper.generators.shared

data class IterPosition(
    var queryParamsIter: MutableMap<String, Iterator<*>>,
    var pathParamsIter: MutableMap<String, Iterator<*>>,
    var headersIter: MutableMap<String, Iterator<*>>,
    var cookiesIter: MutableMap<String, Iterator<*>>,
    var bodiesIter: Iterator<*>
) {
    val queryParams = getValuesMap(queryParamsIter)
    val pathParams = getValuesMap(pathParamsIter)
    val headers = getValuesMap(headersIter)
    val cookies = getValuesMap(cookiesIter)

    fun copy(): IterPosition {
        return IterPosition(
            queryParamsIter.toMutableMap(),
            pathParamsIter.toMutableMap(),
            headersIter.toMutableMap(),
            cookiesIter.toMutableMap(),
            bodiesIter
        )
    }

    private fun getValuesMap(params: MutableMap<String, Iterator<*>>): MutableMap<String, ParamPosition<*>> {
        return params.map { entry -> entry.key to ParamPosition(entry.value) }.toMap().toMutableMap()
    }
}