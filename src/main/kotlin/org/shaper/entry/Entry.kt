package org.shaper.entry

import io.swagger.models.parameters.QueryParameter
import org.shaper.swagger.SpecFinder

fun main(args: Array<String>) {
    args.forEach { println(it) }
    val spec = SpecFinder(args[0], args.slice(1 until args.size).toList())
    val param  = spec.getRelevantSpec().getPath("/jobs").get.parameters[0] as QueryParameter
    println(param.type)
}

