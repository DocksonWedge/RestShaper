package org.shaper.entry

import org.shaper.swagger.SpecFinder

fun main(args: Array<String>) {
    args.forEach { println(it) }
    val spec = SpecFinder(args[0], args.slice(1 until args.size).toList())
    println(spec.endpoints)
}

