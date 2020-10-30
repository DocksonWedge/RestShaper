# RestShaper

## Summary

RestShaper is a project designed to generate API test cases off of swagger documents. The difference with RestShaper is that it seeks to learn the... well, shape of the REST API under test, and alert on changes to that shape. Rather than generating cases purely at random, it aims to 
 * give the user some "steering" for the test case geration
 * inform future test case generation with the results of previous tests
 
 ## Configuration
 
 You can use the RestShaper Kotlin DSL to configure and run a test with one call! You can see examples in SimpleRunner.kt
 ``` fun petStoreGetOrder(numCases: Int = 5): Boolean {
        return runnerConfig {
            inputFunction = SimpleInputGenerator(numCases)::getInput  // You can add custom test gerators
            outputFunction = Results::saveToGlobal // You add a custom output function too
            endpointConfig = {
                swaggerUrl = "https://petstore.swagger.io/v2/swagger.json" // The url or file path of the swagger doc under test
                endpoints = listOf( // The list of all endpoints in swagger to test. 
                    GET to "/store/order/{orderId}"
                )
            }
        }.run()
    } 
```
 
 ## Currently supported
 
 * Query parameter, path parameter, header generation
    * Currently, only String and Integer data types are supported.
 * Failing on 500 errors
 * Serialized results output in JSON
 
 ## Coming soon
 * request body generation
 * additional parameter data types
 * API call "chaining"
 * persistent result storage
 
