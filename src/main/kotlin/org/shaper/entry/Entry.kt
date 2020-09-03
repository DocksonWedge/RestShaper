package org.shaper.entry


import org.shaper.swagger.SpecFinder

//run params http://api.dataatwork.org/v1/spec/skills-api.json GET:/jobs GET:/skills
fun main(args: Array<String>) {
    args.forEach { println(it) }
    val spec = SpecFinder(args[0], args.slice(1 until args.size).toList())
    val endpoints  = spec.getRelevantSpecs()

    endpoints.forEach { it.queryParams.forEach{ println("${it.key} ${it.value.dataType}") }  }


}

