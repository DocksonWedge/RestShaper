package org.shaper.entry

import io.swagger.v3.oas.models.PathItem.HttpMethod.GET
import io.swagger.v3.oas.models.PathItem.HttpMethod.POST
import io.swagger.v3.oas.models.PathItem.HttpMethod.DELETE
import org.shaper.config.runnerConfig
import org.shaper.entry.model.SimpleEndpoint
import org.shaper.generators.SimpleInputGenerator
import org.shaper.generators.model.StaticParams

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
//USED IN HEADER TEST
fun petStoreGetPet(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput
        staticParams = StaticParams(
            headers = mapOf("Authorization" to "token")
        )
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

fun petStoreMultiPet(numCases: Int = 2): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                POST to "/pet",
                DELETE to "/pet/{petId}",
                GET to "/pet/{petId}"
            )
        }
    }.run(3)
}

fun petStorePostOrder(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                POST to "/store/order"
            )
        }
    }.run()
}

fun petStoreCreateWithArray(numCases: Int = 5): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = "https://petstore.swagger.io/v2/swagger.json"
            endpoints = listOf(
                POST to "/user/createWithArray"
            )
        }
    }.run()
}

fun GeneralRun(
    numCases: Int = 5,
    swaggerLocation: String,
    _endpoints: List<SimpleEndpoint>,
    chainDepth: Int = 1
): Boolean {
    return runnerConfig {
        inputFunction = SimpleInputGenerator(numCases)::getInput

        endpointConfig = {
            swaggerUrl = swaggerLocation
            endpoints = _endpoints.map { it.method to it.path }
        }
    }.run(chainDepth)
}
