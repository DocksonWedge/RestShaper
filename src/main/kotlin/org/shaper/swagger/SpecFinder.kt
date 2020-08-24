package org.shaper.swagger

import arrow.core.extensions.list.foldable.combineAll

class SpecFinder(urlOrJson:String, rawEndpoints: List<String> = listOf()) {
    val endpoints = rawEndpoints.map { endpointString ->
        endpointString.split(":").let { it[0] to it[1] }
    }
    fun getFullSpec(){}
    fun getRelevantSpec() {}
}