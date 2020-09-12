package org.shaper.generators.shared

data class IterPosition(
    var queryParams: MutableMap<String, Int>,
    var pathParams: MutableMap<String, Int>,
    var headers: MutableMap<String, Int>,
    var cookies: MutableMap<String, Int>,
    var bodies: Int
) {
    fun copy(): IterPosition {
        return IterPosition(
            queryParams.toMutableMap(),
            pathParams.toMutableMap(),
            headers.toMutableMap(),
            cookies.toMutableMap(),
            bodies
        )
    }
}