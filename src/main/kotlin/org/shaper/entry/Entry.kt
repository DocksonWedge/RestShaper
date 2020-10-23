package org.shaper.entry


import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.BaseTestInput
import org.shaper.generators.model.TestResult
import org.shaper.global.results.Results
import org.shaper.global.results.ResultsStateGlobal
import org.shaper.swagger.SpecFinder
import org.shaper.swagger.model.EndpointSpec
import org.shaper.tester.BaseTestRunner

//run params http://api.dataatwork.org/v1/spec/skills-api.json GET:/jobs GET:/skills
fun main(args: Array<String>) {

    println(dataAtWorkRun())

//    args.forEach { println(it) }
//    val spec = SpecFinder(args[0], args.slice(1 until args.size).toList())
//    val endpoints = spec.getRelevantSpecs()
//
//    endpoints.forEach { it.queryParams.forEach { println("${it.key} ${it.value.dataType}") } }
//
//    BaseTestRunner.shapeEndpoint(
//        endpoints[0],
//        SimpleInputGenerator()::getInput
//    ) { endpoint: EndpointSpec, results: Sequence<TestResult> ->
//
//        Results.saveToGlobal(endpoint, results)
//        println(ResultsStateGlobal.getResultsFromEndpoint(endpoint).map { it.response.statusCode })
////        val iter = results.iterator()
////        iter.next()
////
////        iter.next()
////        iter.next()
////        iter.next()
////        iter.next()
////
////        iter.next()
////        iter.next()
////        iter.next()
////        iter.next()
////        iter.next()
//
//        //results.toList()
//    }

}

