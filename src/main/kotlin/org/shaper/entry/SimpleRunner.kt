package org.shaper.entry

import io.swagger.v3.oas.models.PathItem.HttpMethod.GET
import io.swagger.v3.oas.models.PathItem.HttpMethod.POST
import io.swagger.v3.oas.models.PathItem.HttpMethod.DELETE
import org.shaper.config.runnerConfig
import org.shaper.generators.SimpleInputGenerator

fun dataAtWorkRun(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "http://api.dataatwork.org/v1/spec/skills-api.json"
            endpoints = listOf(
                GET to "/jobs",
                GET to "/skills"
            )
        }
    }.run()
}

fun petStoreGetPet(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                GET to "/pet/{petId}"
            )
        }
    }.run()
}

fun petStoreGetOrderFromFile(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "src\\test\\Resources\\TestExamples\\PetStoreSwaggerEdited.yaml"
            endpoints = listOf(
                GET to "/store/order/{orderId}"
            )
        }
    }.run()
}
fun petStoreGetOrder(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                GET to "/store/order/{orderId}"
            )
        }
    }.run()
}

fun petStoreFindByStatus(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                GET to "/pet/findByStatus"
            )
        }
    }.run()
}
fun petStoreDeletePet(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                DELETE to "/pet/{petId}"
            )
        }
    }.run()
}

fun petStorePostPet(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                POST to "/pet"
            )
        }
    }.run()
}
