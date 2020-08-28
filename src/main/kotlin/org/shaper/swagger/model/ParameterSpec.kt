package org.shaper.swagger.model

import propCheck.arbitrary.Gen
import kotlin.reflect.KClass


class ParameterSpec(val rawType: String) {
    var type: KClass<*> = when (rawType.toLowerCase()) {
        "integer" -> Int::class
        else -> {
            if (rawType.contains("[")) {
                List::class
            } else {
                String::class
            }
        }
    }
    lateinit var customGenerator: Gen<Any> //TODO
    lateinit var GeneratorEnum: List<String> //TODO

}